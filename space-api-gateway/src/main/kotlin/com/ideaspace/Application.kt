package com.ideaspace

import com.ideaspace.config.*
import com.ideaspace.config.configureDocumentEventProducer
import io.ktor.server.application.*
import io.ktor.server.netty.*

fun main(args: Array<String>) {
    EngineMain.main(args)
}

fun Application.module() {
    configureErrorHandling()
    configureHTTP()
    configureDocumentEventProducer()
    configureMonitoring()
    configureSerialization()
    configureDatabases()
    configureSockets()
    configureRouting()
}
