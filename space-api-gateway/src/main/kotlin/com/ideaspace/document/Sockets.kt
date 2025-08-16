package com.ideaspace.document

import com.ideaspace.config.AuthClaims
import com.ideaspace.config.authenticate
import com.ideaspace.core.kafkaMessage.DocumentEventProducer
import com.ideaspace.core.kafkaMessage.DocumentSyncEventValue
import com.ideaspace.core.models.BusinessDocument
import com.ideaspace.core.repository.CrudDocumentRepository
import com.ideaspace.session.DocumentConnection
import com.ideaspace.session.SessionManager
import io.ktor.server.application.*
import io.ktor.server.plugins.di.*
import io.ktor.server.routing.*
import io.ktor.server.websocket.*
import io.ktor.websocket.*
import io.ktor.websocket.CloseReason.Codes.*
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.consumeAsFlow
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.serialization.json.Json
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.polymorphic
import kotlin.time.Duration.Companion.seconds


fun Application.configureSockets() {

    val serializeJson = Json {
        serializersModule = SerializersModule {
            polymorphic(DocumentSyncEventValue::class) {
            }
            // Other useful settings for a robust server
            ignoreUnknownKeys = true
            isLenient = true
        }
    }

    install(WebSockets) {
        pingPeriod = 15.seconds
        timeout = 15.seconds
        maxFrameSize = 64 * 1024 // 64KB max frame size for security
        masking = false
    }

    val sessionManager by lazy { SessionManager() }

    routing {
        webSocket("/ws/documents/{uuid}") {

            // -----------------------------------------------
            // These setups happen once for every connection
            // -----------------------------------------------

            // Authenticate the WebSocket connection
            val authenticatedUser = call.authenticate() ?: return@webSocket close(
                CloseReason(VIOLATED_POLICY, "Authentication failed")
            )

            val docUuid = call.parameters["uuid"] ?: return@webSocket close(
                CloseReason(VIOLATED_POLICY, "Document 'uuid' is required")
            )

            val docEventProducer = dependencies.resolve<DocumentEventProducer>()
            val docRepo = dependencies.resolve<CrudDocumentRepository>()
            val doc = docRepo.findByUuid(docUuid) ?: return@webSocket close(
                CloseReason(VIOLATED_POLICY, "Document not found")
            )

            // Validate document access permissions
            if (!validateDocumentAccess(authenticatedUser, doc)) {
                return@webSocket close(
                    CloseReason(VIOLATED_POLICY, "Access denied to document")
                )
            }

            val connection = DocumentConnection(
                userId = authenticatedUser.user.id,
                docId = doc.id,
                docUuid = doc.uuid,
                session = this
            )
            sessionManager.register(connection, docUuid)

            try {
                // Send a welcome message to confirm successful connection
                send(Frame.Text(Json.encodeToString(mapOf(
                    "type" to "connection_established",
                    "message" to "Successfully connected to document $docUuid",
                    "loginName" to authenticatedUser.user.loginName
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
                println("WebSocket connection error for user ${authenticatedUser.user.id}: ${e.localizedMessage}")
            } finally {
                sessionManager.unregister(connection, docUuid)
                println("WebSocket connection closed for user: ${authenticatedUser.user.loginName} (ID: ${authenticatedUser.user.id})")
            }
        }
    }
}


/**
 * Validates that a user has permission to access a specific document
 */
fun validateDocumentAccess(authClaims: AuthClaims, document: BusinessDocument): Boolean {

    // TODO: Implement proper authorization logic
    // Examples of checks you might want to add:
    // - Is the user the owner of the document?
    // - Is the user a member of the team/workspace that owns the document?
    // - Does the user have specific permissions (read/write) for this document?
    // - Is the document public or private?

    // For now, return true if document exists and user is authenticated
    // In production, implement your specific business logic here

    println("Document access granted for user ${authClaims.user.loginName} to document")

    return true
}