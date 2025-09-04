package com.ideaspace.peerManager

import io.ktor.server.application.*
import io.ktor.server.plugins.di.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.runBlocking

fun Application.configureWebRTC() {
    dependencies.provide<RTCPeerManager> {
        RedisPeerManagerImpl(
            sessionManager = resolve(),
            redisClient = resolve(),
            scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
        )
    }

    monitor.subscribe(ApplicationStopping) {
        runBlocking {
            dependencies.resolve<RTCPeerManager>().close()
        }
    }
}
