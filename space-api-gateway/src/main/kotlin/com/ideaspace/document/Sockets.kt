package com.ideaspace.document

import com.ideaspace.core.kafkaMessage.DocumentSyncEventValue
import com.ideaspace.core.repository.CrudDocumentRepository
import com.ideaspace.core.kafkaMessage.DocumentEventProducer
import com.ideaspace.session.DocumentConnection
import com.ideaspace.session.SessionManager
import io.ktor.server.application.*
import io.ktor.server.plugins.di.dependencies
import io.ktor.server.routing.routing
import io.ktor.server.websocket.*
import io.ktor.websocket.CloseReason
import io.ktor.websocket.Frame
import io.ktor.websocket.close
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.consumeAsFlow
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.serialization.json.Json
import kotlinx.serialization.modules.SerializersModule
import kotlin.time.Duration.Companion.seconds
import io.ktor.websocket.readText
import kotlinx.serialization.modules.polymorphic


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
        maxFrameSize = Long.MAX_VALUE
        masking = false
    }

    val sessionManager by lazy { SessionManager() }

    routing {
        webSocket("/ws/documents/{uuid}") {
            val docUuid = call.parameters["uuid"] ?: return@webSocket close(
                CloseReason(
                    CloseReason.Codes.VIOLATED_POLICY,
                    "boardId is required"
                )
            )
            val connection = DocumentConnection(userId = 1, session = this)
            sessionManager.register(connection, docUuid)

            try {
                incoming.consumeAsFlow().mapNotNull { frame ->
                    if (frame is Frame.Text) {
                        try {
                            println("Received frame: ${frame.readText()}")
                            val event = Json.decodeFromString<DocumentSyncEventValue>(frame.readText())
                            val documentEventProducer = call.application.dependencies.resolve<DocumentEventProducer>()
                            val dao = application.dependencies.resolve<CrudDocumentRepository>().findByIdUuid(uuid = docUuid)
                            documentEventProducer.sendEvent(dao.id.value, event)
                            println("Event sent successfully")
                        } catch (e: Exception) {
                            // Log the error
                            println("Error deserializing frame: ${e.localizedMessage}")
                        }
                    }
                }.collect()
            } finally {
                sessionManager.unregister(connection, docUuid)
            }

        }
    }
}
