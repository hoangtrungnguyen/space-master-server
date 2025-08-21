package com.ideaspace.config

import dev.hayden.KHealth
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.plugins.calllogging.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.plugins.cors.routing.*
import io.ktor.server.plugins.swagger.*
import io.ktor.server.request.*
import io.ktor.server.routing.*
import io.ktor.util.*
import org.slf4j.event.Level

fun Application.configureHttpServer() {
    install(CORS) {
        allowHost("localhost:5173", schemes = listOf("http", "httpss"))

        // This is the crucial part that works with `credentials: 'include'`
        allowCredentials = true

        allowMethod(HttpMethod.Options)
        allowMethod(HttpMethod.Put)
        allowMethod(HttpMethod.Delete)
        allowMethod(HttpMethod.Patch)

        allowHeader(HttpHeaders.ContentType)
        allowHeader(HttpHeaders.Authorization)

        anyHost() // @TODO: Don't do this in production if possible. Try to limit it.
    }
    install(ContentNegotiation) {
        json()
    }
    install(CallLogging) {
        level = Level.INFO // Set the logging level (e.g., INFO, DEBUG, TRACE)
        filter { call -> call.request.path().startsWith("/") } // Optional: filter requests to log
        format { call -> // Optional: customize the log message format
            val status = call.response.status()
            val httpMethod = call.request.httpMethod.value
            val path = call.request.uri
            """Status: $status, Method: $httpMethod, Path: $path
                |Headers: ${call.request.headers.toMap()}
                |Cookies: ${call.request.cookies.rawCookies}
            """.trimMargin()
        }
    }
    install(KHealth)
    routing {
        swaggerUI(path = "openapi")
    }
}
