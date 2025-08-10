package com.space


import com.space.features.space.whiteBoardRoutes
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.plugins.calllogging.*
import io.ktor.server.plugins.cors.routing.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Application.configureRouting() {
    install(CORS) {
        allowMethod(HttpMethod.Options)
        allowMethod(HttpMethod.Put)
        allowMethod(HttpMethod.Delete)
        allowMethod(HttpMethod.Patch)

        allowHeader(HttpHeaders.ContentType)
        allowHeader(HttpHeaders.Authorization)

        anyHost() // @TODO: Don't do this in production if possible. Try to limit it.
    }
    install(CallLogging)
    routing {
        whiteBoardRoutes()
        get("/") {
            call.respondText("Hello World!")
        }
    }

}
