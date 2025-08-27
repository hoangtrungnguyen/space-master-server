package com.ideaspace.peerManager

import io.ktor.server.application.*
import io.ktor.server.plugins.di.*

fun Application.configureWebRTC() {
    dependencies.provide<RTCPeerManager> {
        InMemoryRTCPeerManagerImpl(
            resolve()
        )
    }
}
