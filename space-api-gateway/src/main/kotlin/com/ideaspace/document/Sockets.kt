@file:OptIn(ExperimentalTime::class)

package com.ideaspace.document

import com.ideaspace.session.SessionManager
import io.ktor.serialization.kotlinx.*
import io.ktor.server.application.*
import io.ktor.server.plugins.di.*
import io.ktor.server.websocket.*
import io.lettuce.core.api.StatefulRedisConnection
import io.lettuce.core.pubsub.StatefulRedisPubSubConnection
import kotlinx.serialization.json.Json
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

    val redis = dependencies.resolve<StatefulRedisConnection<String, String>>("redis-string-string-connection")
    val redisStringBytes = dependencies.resolve<StatefulRedisConnection<String, ByteArray>>("redis-string-bytes-connection")
    val redisPubSub = dependencies.resolve<StatefulRedisPubSubConnection<String, String>>("redis-pub-sub-connection")

    dependencies {
        provide<SessionManager> { SessionManager(redisStringBytes, redisPubSub) }
        provide<PullStreamContext> { PullStreamContext(redisStringBytes, redisPubSub) }
    }
}
