package com.ideaspace

import com.ideaspace.config.*
import com.ideaspace.core.kafkaMessage.configureDocumentEventProducer
import com.ideaspace.document.configureSockets
import io.ktor.server.application.*
import io.ktor.server.netty.*

fun main(args: Array<String>) {
    EngineMain.main(args)
}

fun Application.module() {
    configureErrorHandling()
    configureHTTP()
    configureSecurity()
    configureDocumentEventProducer()
    configureDatabases()
    configureRouting()
    configureSockets()
}
