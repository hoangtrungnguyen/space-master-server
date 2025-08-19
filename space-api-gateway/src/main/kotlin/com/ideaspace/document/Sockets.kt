@file:OptIn(ExperimentalTime::class)

package com.ideaspace.document

import com.ideaspace.session.SessionManager
import io.ktor.server.application.*
import io.ktor.server.plugins.di.*
import io.ktor.server.websocket.*
import io.lettuce.core.RedisClient
import kotlin.time.Duration.Companion.seconds
import kotlin.time.ExperimentalTime


suspend fun Application.configureSockets() {
    install(WebSockets) {
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
