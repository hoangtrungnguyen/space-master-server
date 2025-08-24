package com.ideaspace.rtcmanager

import io.ktor.server.application.*
import io.ktor.server.plugins.di.*

fun Application.configureWebRTC() {
    dependencies.provide<RTCManager> {
        RTCManager()
    }
}
