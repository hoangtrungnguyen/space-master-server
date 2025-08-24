@file:OptIn(ExperimentalTime::class)

package com.ideaspace.document

import com.ideaspace.config.AuthPrincipal
import com.ideaspace.core.kafkaMessage.DocumentEventProducer
import com.ideaspace.core.kafkaMessage.DocumentSyncEventValue
import com.ideaspace.core.models.BusinessDocument
import com.ideaspace.core.models.Process
import com.ideaspace.core.models.ProcessKey
import com.ideaspace.core.repository.CrudDocumentRepository
import com.ideaspace.core.repository.ProcessRepo
import com.ideaspace.rtcmanager.RTCData
import com.ideaspace.rtcmanager.RTCManager
import com.ideaspace.rtcmanager.RTCService
import com.ideaspace.session.DocumentConnection
import com.ideaspace.session.SessionManager
import io.ktor.http.*
import io.ktor.server.auth.*
import io.ktor.server.plugins.di.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.websocket.*
import io.ktor.websocket.*
import io.ktor.websocket.CloseReason.Codes.*
import kotlinx.coroutines.flow.*
import kotlinx.serialization.json.Json
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

fun Route.documentManagementRoutes() {
    authenticate("auth-session") {

        post("/api/documents/create") {
            val request = call.receive<CreateDocumentRequest>()

            val command = CreateDocumentCommand(request)
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

        get("/api/documents/{uuid}") {

        }
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
            val rtcManager = d.resolve<RTCManager>()

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


            // docId, userId, windowId

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

            val webSocketUri = call.request.uri
            val port = call.request.port()
            rtcManager.add(
                doc.id, RTCData(
                    port,
                    webSocketUri
                )
            )

            sessionManager.broadCastPeer(doc.id)

            val rtcService = RTCService()

            try {
                // Send a welcome message to confirm successful connection
                send(
                    Frame.Text(
                        Json.encodeToString(
                            mapOf(
                                "type" to "connection_established",
                                "message" to "Successfully connected to document $docUuid",
                                "loginName" to principal.user.loginName
                            )
                        )
                    )
                )

                val messageFlow: Flow<Any> = incoming.consumeAsFlow().mapNotNull { frame ->
                    if (frame is Frame.Text) {
                        try {
                            val frameText = frame.readText()
                            val event = Json.decodeFromString<DocumentSyncEventValue>(frameText)
                            send(
                                Frame.Text(
                                    Json.encodeToString(
                                        mapOf(
                                            "type" to "success",
                                            "message" to "Received event from process ${event.processId}"
                                        )
                                    )
                                )
                            )
                            return@mapNotNull event
                        } catch (e: Exception) {
                            e.printStackTrace()
                            try {
                                send(
                                    Frame.Text(
                                        Json.encodeToString(
                                            mapOf(
                                                "type" to "error",
                                                "message" to "Failed to process message: ${e.localizedMessage}"
                                            )
                                        )
                                    )
                                )
                            } catch (sendError: Exception) {
                                println("Failed to send error response: ${sendError.localizedMessage}")
                            }
                        }
                    }
                }.stateIn(this)

                rtcService.transform(
                    this,
                    messageFlow
                        .filterIsInstance<DocumentSyncEventValue>(),
                    { editDocEvent
                        ->
                        println("edit doc event sent to sync server. doc.id=${doc.id}")
                        docEventProducer.sendEvent(doc.id, editDocEvent)
                    }, { initSyncEventValue ->
                        println("init doc event sent to sync server. doc.id=${doc.id}")
                        docEventProducer.sendEvent(doc.id, initSyncEventValue)
                    })
            } catch (e: Exception) {
                println("WebSocket connection error for user $userId: ${e.localizedMessage}")
            } finally {
                rtcManager.remove(doc.id, webSocketUri)
                sessionManager.unregister(processKey)
                rtcService.close()
                println("WebSocket connection closed for user: ${principal.user.loginName} (ID: $userId)")
            }
        }
    }
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