@file:OptIn(ExperimentalTime::class)

package com.ideaspace.document

import com.ideaspace.config.AuthPrincipal
import com.ideaspace.core.kafkaMessage.DocumentEventProducer
import com.ideaspace.core.kafkaMessage.DocumentSyncEventValue
import com.ideaspace.core.kafkaMessage.InitSyncEventValue
import com.ideaspace.core.models.BusinessDocument
import com.ideaspace.core.models.Process
import com.ideaspace.core.models.ProcessKey
import com.ideaspace.core.repository.CrudDocumentRepository
import com.ideaspace.core.repository.ProcessRepo
import com.ideaspace.rtcmanager.RTCPeerManager
import com.ideaspace.session.DocumentConnection
import com.ideaspace.session.SessionManager
import io.ktor.serialization.kotlinx.KotlinxWebsocketSerializationConverter
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.plugins.di.*
import io.ktor.server.routing.*
import io.ktor.server.websocket.*
import io.ktor.websocket.*
import io.ktor.websocket.CloseReason.Codes.*
import io.lettuce.core.RedisClient
import kotlinx.serialization.json.Json
import kotlinx.coroutines.flow.*
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json
import kotlin.random.Random
import kotlin.time.Clock
import kotlin.time.Duration.Companion.seconds
import kotlin.time.ExperimentalTime


suspend fun Application.configureSockets() {
    install(WebSockets) {
        contentConverter = KotlinxWebsocketSerializationConverter(Json)
        pingPeriod = 15.seconds
        timeout = 15.seconds
        maxFrameSize = 64 * 1024 // 64KB max frame size for security
        masking = false
    }

    val redisClient = dependencies.resolve<RedisClient>()

    dependencies {
        provide<SessionManager> { SessionManager(redisClient) }
    }


}

fun Route.documentWebSocketRoutes() {
    /**
     * This websocket allow connecting to stream of changes of specific [BusinessDocument]
     * via its [BusinessDocument.uuid].
     *
     * Client (Desktop app or Browser tab) must provide their generated Window Id
     * via `wid` request parameter.
     */

    authenticate("auth-session") {
        val d = application.dependencies

        webSocket("/ws/documents/{uuid}") {
            val sessionManager = d.resolve<SessionManager>()
            val docEventProducer = d.resolve<DocumentEventProducer>()
            val docRepo = d.resolve<CrudDocumentRepository>()
            val processRepo = d.resolve<ProcessRepo>()
            val rtcPeerManager = d.resolve<RTCPeerManager>()

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

            var process = processRepo.findByKey(processKey) ?: processRepo.create(
                Process(
                    id = -1,
                    docId = doc.id,
                    userId = userId,
                    windowId = windowId,
                    sessionId = Random(Int.MAX_VALUE).nextLong(),
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

            val messageFlow: Flow<Any> =
                incoming.consumeAsFlow()
                    .filterIsInstance<Frame.Text>() // Process only text frames
                    .mapNotNull { frame ->
                        val frameText = frame.readText()
                        var event = Json.decodeFromString<DocumentSyncEventValue>(frameText)
                        if (event is InitSyncEventValue) {
                            process = process.copy(
                                peerUuid = event.payload.peerUuid
                            )
                            rtcPeerManager.publishPeer(process)
                            event = event.copy(
                                docId = doc.id,
                                userId = userId,
                            )
                        }
                        docEventProducer.sendEvent(doc.id, event)
//                        send(Frame.Text("Sending event $event"))
                        println("Sent event for $docUuid. Event: $event to REDIS")
                        event
                    }.catch { cause ->
                        try {
                            if (cause is SerializationException) {
                                send(
                                    Frame.Text(
                                        Json.encodeToString(
                                            mapOf(
                                                "type" to "error",
                                                "message" to "Failed to process message: ${cause.localizedMessage}"
                                            )
                                        )
                                    )
                                )
                            } else {
                                cause.printStackTrace()
                                println("Exception: ${cause.localizedMessage}")
                            }
                        } catch (t: Exception) {
                            t.printStackTrace()
                            throw t
                        }
                    }.onCompletion { cause ->
                        // This block executes when the flow is complete for any reason.
                        // 'cause' will be null on a normal close, or an exception on an error close.
                        println("Flow completed. Cleaning up connection...")
                        if (cause != null) {
                            println("Reason: ${cause.localizedMessage}")
                        }
                        sessionManager.unregister(processKey)
                        rtcPeerManager.removePeer(process)
                        println("WebSocket cleanup finished for user: ${principal.user.loginName}")
                    }

            messageFlow.launchIn(this)
            closeReason.await()
        }
    }


}
