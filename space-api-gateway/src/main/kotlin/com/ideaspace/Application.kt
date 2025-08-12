package com.space.com.ideaspace

import com.space.com.ideaspace.config.configureDatabases
import com.space.com.ideaspace.config.configureFrameworks
import com.space.com.ideaspace.config.configureHTTP
import com.space.com.ideaspace.config.configureMonitoring
import com.space.com.ideaspace.config.configureRouting
import com.space.com.ideaspace.config.configureSerialization
import com.space.com.ideaspace.config.configureSockets
import com.space.com.ideaspace.present.configureAdministration
import io.ktor.server.application.*
import io.ktor.server.netty.EngineMain

fun main(args: Array<String>) {
    EngineMain.main(args)
}

fun Application.module() {
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
