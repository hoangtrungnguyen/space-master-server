package com.ideaspace

import com.ideaspace.config.*
import com.ideaspace.core.kafkaMessage.configureDocumentEventProducer
import com.ideaspace.document.configureSockets
import com.ideaspace.document.documentManagementRoutes
import com.ideaspace.document.documentWebSocketRoutes
import com.ideaspace.rtcmanager.configureWebRTC
import com.ideaspace.user.userManagementRoutes
import io.ktor.server.application.*
import io.ktor.server.netty.*
import io.ktor.server.routing.*

fun main(args: Array<String>) {
    EngineMain.main(args)
}

suspend fun Application.module() {
    configureErrorHandling()
    configureHttpServer()
    configureDatabases()
    configureRedis()
    configureSecurity()
    configureDocumentEventProducer()
    configureSockets()
    configureWebRTC()
    routing {
        authRoutes()
        userManagementRoutes()
        documentManagementRoutes()
        documentWebSocketRoutes()
    }
}
