package com.space.com.ideaspace.config

import io.ktor.serialization.kotlinx.KotlinxWebsocketSerializationConverter
import io.ktor.server.application.*
import io.ktor.server.websocket.*
import kotlinx.serialization.json.Json
import kotlinx.serialization.modules.SerializersModule
import kotlin.time.Duration.Companion.seconds

fun Application.configureSockets() {
    val customJson = Json {
        serializersModule = SerializersModule {
//            polymorphic(Operation::class) {
                // Use @SerialName in your data classes for stable names.
//                subclass(Operation.DocumentOperation::class)
//            }
        }
        // Other useful settings for a robust server
        ignoreUnknownKeys = true
        isLenient = true
    }

    install(WebSockets) {
        pingPeriod = 15.seconds
        timeout = 15.seconds
        maxFrameSize = Long.MAX_VALUE
        masking = false
        contentConverter = KotlinxWebsocketSerializationConverter(customJson)
    }
}
