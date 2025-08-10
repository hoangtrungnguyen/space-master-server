package com.space

import io.ktor.server.application.*

fun main(args: Array<String>) {
    io.ktor.server.netty.EngineMain.main(args)
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
