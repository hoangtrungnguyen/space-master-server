package com.ideaspace.peerManager

import io.ktor.server.application.*
import io.ktor.server.plugins.di.*
import kotlinx.coroutines.runBlocking

fun Application.configureWebRTC() {
    dependencies.provide<RTCPeerManager> {
        InMemoryPeerManager(
            sessionManager = resolve()
        )
    }

    monitor.subscribe(ApplicationStopping) {
        runBlocking {
            dependencies.resolve<RTCPeerManager>().close()
        }
    }
}
