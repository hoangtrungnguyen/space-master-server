package com.ideaspace.config

import com.ideaspace.document.documentManagementRoutes
import com.ideaspace.user.userManagementRoutes
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.plugins.calllogging.*
import io.ktor.server.plugins.cors.routing.*
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

    routing {
        authRoutes()
        userManagementRoutes()
        documentManagementRoutes()
    }

}
