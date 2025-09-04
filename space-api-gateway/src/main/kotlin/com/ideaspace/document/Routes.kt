@file:OptIn(ExperimentalTime::class)

package com.ideaspace.document

import com.ideaspace.config.AuthPrincipal
import com.ideaspace.config.ErrorResponse
import com.ideaspace.core.kafkaMessage.DocumentEventProducer
import com.ideaspace.core.models.BusinessDocument
import com.ideaspace.core.models.Process
import com.ideaspace.core.models.ProcessKey
import com.ideaspace.core.repository.CrudDocumentRepository
import com.ideaspace.core.repository.ElementRepo
import com.ideaspace.core.repository.ProcessRepo
import com.ideaspace.session.*
import io.ktor.http.*
import io.ktor.server.auth.*
import io.ktor.server.plugins.di.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.websocket.*
import io.ktor.websocket.*
import io.ktor.websocket.CloseReason.Codes.*
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.consumeAsFlow
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.serialization.json.Json
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

val logger: Logger = LoggerFactory.getLogger("documentManagementRoutes")

fun Route.documentManagementRoutes() {
    application.dependencies.provide<GetDocumentContext> {
        val docRepo = resolve<CrudDocumentRepository>()
        val elementRepo = resolve<ElementRepo>()
        GetDocumentContext(docRepo, elementRepo)
    }

    authenticate("auth-session") {

        post("/api/documents/create") {
            val principal = call.principal<AuthPrincipal>()!!
            val request = call.receive<CreateDocumentRequest>()

            val command = CreateDocumentCommand(principal, request)
            val result = command.execute(application.dependencies)

            call.respond(status = HttpStatusCode.OK, result)
        }

        delete("/api/documents/{id}") {

        }

        get("/api/documents") {
            val principal = call.principal<AuthPrincipal>()!!
            val user = principal.user

            val command = GetAllDocument(
                user.id,
                application.dependencies.resolve()
            )
            val result = command.execute()
            call.respond(status = HttpStatusCode.OK, result)
        }

        get("/api/documents/{uuid}", RoutingContext::getDocumentQuery)
    }

}

/**
 * This websocket allow connecting to stream of changes of specific [BusinessDocument]
 * via its [BusinessDocument.uuid].
 *
 * Client (Desktop app or Browser tab) must provide their generated Window Id
 * via `wid` request parameter.
 */
fun Route.documentChangeRoutes() {

    authenticate("auth-session") {
        val d = application.dependencies


        webSocket("/ws/documents/{uuid}") {
            val sessionManager = d.resolve<SessionManager>()
            val docEventProducer = d.resolve<DocumentEventProducer>()
            val docRepo = d.resolve<CrudDocumentRepository>()
            val processRepo = d.resolve<ProcessRepo>()
            val pullStreamContext = d.resolve<PullStreamContext>()


            // -----------------------------------------------
            // These setups happen once for every connection
            // -----------------------------------------------

            val principal = call.principal<AuthPrincipal>() ?: return@webSocket close(
                CloseReason(VIOLATED_POLICY, "Authentication failed")
            )

            val docUuid = call.parameters["uuid"] ?: return@webSocket close(
                CloseReason(VIOLATED_POLICY, "Document 'uuid' is required")
            )

            val doc = docRepo.findByUuid(docUuid) ?: return@webSocket close(
                CloseReason(VIOLATED_POLICY, "Document not found")
            )

            if (!validateDocumentAccess(principal, doc)) {
                return@webSocket close(
                    CloseReason(VIOLATED_POLICY, "Access denied to document")
                )
            }

            val windowId = call.parameters["wid"]?.toLong() ?: return@webSocket close(
                CloseReason(VIOLATED_POLICY, "Window 'wid' is required")
            )

            val userId = principal.user.id
            val processKey = ProcessKey(doc.id, userId, windowId)
            val process = processRepo.findByKey(processKey) ?: processRepo.create(
                Process(
                    id = -1,
                    docId = doc.id,
                    userId = userId,
                    windowId = windowId,
                    sessionId = -1,
                    isActive = true,
                    lastActiveAt = Clock.System.now()
                )
            )

            sessionManager.register(
                processKey, DocumentConnection(
                    process = process,
                    webSocket = this
                )
            )

            try {
                // Send a welcome message to confirm successful connection
                sendSerialized(ConnectedOutput(
                    replyTo = "NONE",
                    message = "Successfully connected to document $docUuid",
                    loginName = principal.user.loginName
                ))

                incoming.consumeAsFlow().mapNotNull { frame ->
                    if (frame is Frame.Text) {
                        var fallbackMessageId: String? = null
                        var fallbackMessageType: String? = null

                        try {
                            val frameText = frame.readText()
                            fallbackMessageId = extractMessageId(frameText)
                            fallbackMessageType = extractMessageType(frameText)
                            val input = ChannelJson.decodeFromString<DocumentChannelInput>(frameText)
                            when (input) {
                                is DocumentFlowUpChange -> {
                                    docEventProducer.sendEvent(doc.id, input.toDocumentSyncEventValue(process))
                                    sendSerialized(Acknowledgement(
                                        replyTo = input.messageId,
                                        message = "Received event ${input.messageType} from window ${process.windowId}"
                                    ))
                                }

                                is PullStreamInput -> {
                                    val response = PullStreamCommand(process, input)
                                        .execute(pullStreamContext)
                                    sendSerialized(response)
                                }
                            }
                        } catch (consumeError: Exception) {
                            logger.error("Failed to consume message ${fallbackMessageId} type ${fallbackMessageType}", consumeError)
                            try {
                                sendSerialized(ErrorOutput(
                                    replyTo = fallbackMessageId ?: "NONE",
                                    error = ErrorResponse(
                                        code = "INVALID_JSON_FORMAT",
                                        message = "Unable to parse request body",
                                        details = consumeError.message,
                                        timestamp = System.currentTimeMillis()
                                    )
                                ))
                            } catch (replyError: Exception) {
                                logger.error("Failed to send error response", replyError)
                            }
                        }
                    }
                }.collect()


            } catch (e: Exception) {
                logger.error("WebSocket connection error for user $userId: ${e.localizedMessage}", e)
            } finally {
                sessionManager.unregister(processKey)
                logger.info("WebSocket connection closed for user: ${principal.user.loginName} (ID: $userId)")
            }
        }
    }
}

fun extractMessageId(json: String): String? {
    val regex = """"messageId"\s*:\s*"([^"]*)"""".toRegex()
    return regex.find(json)?.groupValues?.get(1)
}

fun extractMessageType(json: String): String? {
    val regex = """"messageType"\s*:\s*"([^"]*)"""".toRegex()
    return regex.find(json)?.groupValues?.get(1)
}

/**
 * Validates that a user has permission to access a specific document
 */
fun validateDocumentAccess(authPrincipal: AuthPrincipal, document: BusinessDocument): Boolean {

    // TODO: Implement proper authorization logic
    // Examples of checks you might want to add:
    // - Is the user the owner of the document?
    // - Is the user a member of the team/workspace that owns the document?
    // - Does the user have specific permissions (read/write) for this document?
    // - Is the document public or private?

    // For now, return true if document exists and user is authenticated
    // In production, implement your specific business logic here

    println("Document access granted for user ${authPrincipal.user.loginName} to document")

    return true
}