package com.ideaspace

import com.ideaspace.config.configureDatabases
import com.ideaspace.config.configureErrorHandling
import com.ideaspace.config.configureHTTP
import com.ideaspace.config.configureLogging
import com.ideaspace.config.configureMonitoring
import com.ideaspace.config.configureRedisRoute
import com.ideaspace.config.configureRouting
import com.ideaspace.config.configureSerialization
import com.ideaspace.config.configureServerKafka
import com.ideaspace.document.provideDocumentDI
import io.ktor.server.application.*
import io.ktor.server.netty.EngineMain

fun main(args: Array<String>) {
    EngineMain.main(args)
}

fun Application.module() {
    configureErrorHandling()
    configureHTTP()
    configureMonitoring()
    configureSerialization()
    configureDatabases()
    configureRouting()
    configureRedisRoute()
    configureLogging()

    provideDocumentDI()

    configureServerKafka()
}