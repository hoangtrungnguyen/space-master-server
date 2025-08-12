package com.ideaspace

import com.ideaspace.config.configureDatabases
import com.ideaspace.config.configureErrorHandling
import com.ideaspace.config.configureFrameworks
import com.ideaspace.config.configureHTTP
import com.ideaspace.config.configureMonitoring
import com.ideaspace.config.configureRouting
import com.ideaspace.config.configureSerialization
import com.ideaspace.config.configureSockets
import com.ideaspace.present.configureAdministration
import com.ideaspace.config.configureMonitoring
import io.ktor.server.application.*
import io.ktor.server.netty.EngineMain

fun main(args: Array<String>) {
    EngineMain.main(args)
}

fun Application.module() {
    configureErrorHandling()
    configureHTTP()
//    configureSecurity()
    configureMonitoring()
    configureSerialization()
    configureDatabases()
    configureFrameworks()
    configureSockets()
    configureAdministration()
    configureRouting()
}
