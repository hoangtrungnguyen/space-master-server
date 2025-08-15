package com.ideaspace.config

import dev.hayden.KHealth
import io.ktor.serialization.kotlinx.json.json
import io.ktor.server.application.*
import io.ktor.server.plugins.calllogging.CallLogging
import io.ktor.server.plugins.contentnegotiation.ContentNegotiation
import io.ktor.server.plugins.swagger.*
import io.ktor.server.routing.*

fun Application.configureHTTP() {
    install(ContentNegotiation) {
        json()
    }
    install(CallLogging)
    install(KHealth)
    routing {
        swaggerUI(path = "openapi")
    }
}
