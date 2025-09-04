@file:OptIn(ExperimentalTime::class)

package com.ideaspace.document

import com.ideaspace.config.AuthPrincipal
import com.ideaspace.core.kafkaMessage.DocumentEventProducer
import com.ideaspace.core.models.BusinessDocument
import com.ideaspace.core.models.Process
import com.ideaspace.core.models.ProcessKey
import com.ideaspace.core.repository.CrudDocumentRepository
import com.ideaspace.core.repository.ProcessRepo
import com.ideaspace.peerManager.PeerUuid
import com.ideaspace.peerManager.RTCPeerManager
import com.ideaspace.session.*
import io.ktor.serialization.kotlinx.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.plugins.di.*
import io.ktor.server.routing.*
import io.ktor.server.websocket.*
import io.ktor.websocket.*
import io.ktor.websocket.CloseReason.Codes.*
import io.lettuce.core.api.StatefulRedisConnection
import io.lettuce.core.pubsub.StatefulRedisPubSubConnection
import kotlinx.coroutines.flow.*
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json
import kotlin.random.Random
import kotlin.time.Clock
import kotlin.time.Duration.Companion.seconds
import kotlin.time.ExperimentalTime


suspend fun Application.configureSockets() {
    install(WebSockets) {
        contentConverter = KotlinxWebsocketSerializationConverter(Json {
            encodeDefaults = true
        })
        pingPeriod = 15.seconds
        timeout = 15.seconds
        maxFrameSize = 64 * 1024 // 64KB max frame size for security
        masking = false
    }

    val redis = dependencies.resolve<StatefulRedisConnection<String, String>>("redis-string-string-connection")
    val redisStringBytes = dependencies.resolve<StatefulRedisConnection<String, ByteArray>>("redis-string-bytes-connection")
    val redisPubSub = dependencies.resolve<StatefulRedisPubSubConnection<String, String>>("redis-pub-sub-connection")

    dependencies {
        provide<SessionManager> { SessionManager(redisStringBytes, redisPubSub) }
        provide<PullStreamContext> { PullStreamContext(redisStringBytes, redisPubSub) }
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

            var peerUuid: PeerUuid? = null
            val messageFlow: Flow<Any> =
                incoming.consumeAsFlow()
                    .filterIsInstance<Frame.Text>() // Process only text frames
                    .mapNotNull { frame ->
                        val frameText = frame.readText()
                        val input = Json.decodeFromString<DocumentChannelInput>(frameText)
                        when (input) {
                            is DocumentFlowUpChange -> {

                                if (input is InitSyncInput) {
                                    if (input.peerUuid == null) {
                                        println("⚠️ Process $processKey with event $input doesn't have peer uuid ")
                                    } else {
                                        rtcPeerManager.registerPeerGroup(
                                            docId = processKey.docId,
                                            peerUuid = input.peerUuid,
                                        )
                                        peerUuid = input.peerUuid
                                    }
                                }

                                val docEvent = input.toDocumentSyncEventValue(process)
                                println(docEvent)
                                docEventProducer.sendEvent(doc.id, docEvent)

                                sendSerialized(
                                    Acknowledgement(
                                        replyTo = input.messageId,
                                        message = "Received event ${input.messageType} from window ${process.windowId}"
                                    )
                                )
                            }

                            is PullStreamInput -> {
                                val response = PullStreamCommand(process, input)
                                    .execute(pullStreamContext)
                                sendSerialized(response)
                            }
                        }

                        input
                    }.catch { cause ->
                        try {
                            if (cause is SerializationException) {
                                cause.printStackTrace()
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
                        peerUuid?.let {
                            rtcPeerManager.unregisterPeerGroup(processKey.docId, it)
                        } ?: println("OnSocket Complete: Peer not found")
                        println("WebSocket cleanup finished for user: ${principal.user.loginName}")
                    }

            messageFlow.launchIn(this)
            closeReason.await()
        }
    }

}

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
