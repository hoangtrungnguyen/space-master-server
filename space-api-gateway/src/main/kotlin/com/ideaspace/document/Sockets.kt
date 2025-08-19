@file:OptIn(ExperimentalTime::class)

package com.ideaspace.document

import com.ideaspace.config.AuthPrincipal
import com.ideaspace.core.kafkaMessage.DocumentEventProducer
import com.ideaspace.core.kafkaMessage.DocumentSyncEventValue
import com.ideaspace.core.models.BusinessDocument
import com.ideaspace.core.models.Process
import com.ideaspace.core.repository.CrudDocumentRepository
import com.ideaspace.core.repository.ProcessRepo
import com.ideaspace.session.DocumentConnection
import com.ideaspace.session.SessionManager
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.plugins.di.*
import io.ktor.server.routing.*
import io.ktor.server.websocket.*
import io.ktor.websocket.*
import io.ktor.websocket.CloseReason.Codes.*
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.consumeAsFlow
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.serialization.json.Json
import kotlin.time.Clock
import kotlin.time.Duration.Companion.seconds
import kotlin.time.ExperimentalTime


suspend fun Application.configureSockets() {

    install(WebSockets) {
        pingPeriod = 15.seconds
        timeout = 15.seconds
        maxFrameSize = 64 * 1024 // 64KB max frame size for security
        masking = false
    }

    val sessionManager by lazy { SessionManager() }
    val docEventProducer = dependencies.resolve<DocumentEventProducer>()
    val docRepo = dependencies.resolve<CrudDocumentRepository>()
    val processRepo = dependencies.resolve<ProcessRepo>()

    routing {
        authenticate("jwt-auth") {
            webSocket("/ws/documents/{uuid}") {

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
                val process = processRepo.findByDocUserWindow(doc.id, userId, windowId) ?: processRepo.create(Process(
                    id = -1,
                    docId = doc.id,
                    userId = userId,
                    windowId = windowId,
                    sessionId = -1,
                    isActive = true,
                    lastActiveAt = Clock.System.now()
                ))


                val connection = DocumentConnection(
                    docUuid = doc.uuid,
                    process = process,
                    session = this
                )
                sessionManager.register(connection, docUuid)

                try {
                    // Send a welcome message to confirm successful connection
                    send(Frame.Text(Json.encodeToString(mapOf(
                        "type" to "connection_established",
                        "message" to "Successfully connected to document $docUuid",
                        "loginName" to principal.user.loginName
                    ))))

                    incoming.consumeAsFlow().mapNotNull { frame ->
                        if (frame is Frame.Text) {
                            try {
                                val frameText = frame.readText()
                                val event = Json.decodeFromString<DocumentSyncEventValue>(frameText)
                                docEventProducer.sendEvent(doc.id, event)

                                send(Frame.Text(Json.encodeToString(mapOf(
                                    "type" to "success",
                                    "message" to "Received event from process ${event.processId}"
                                ))))

                            } catch (e: Exception) {
                                e.printStackTrace()
                                try {
                                    send(Frame.Text(Json.encodeToString(mapOf(
                                        "type" to "error",
                                        "message" to "Failed to process message: ${e.localizedMessage}"
                                    ))))
                                } catch (sendError: Exception) {
                                    println("Failed to send error response: ${sendError.localizedMessage}")
                                }
                            }
                        }
                    }.collect()


                } catch (e: Exception) {
                    println("WebSocket connection error for user $userId: ${e.localizedMessage}")
                } finally {
                    sessionManager.unregister(connection, docUuid)
                    println("WebSocket connection closed for user: ${principal.user.loginName} (ID: $userId)")
                }
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