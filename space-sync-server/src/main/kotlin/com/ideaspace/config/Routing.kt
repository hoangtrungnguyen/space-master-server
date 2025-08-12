package com.ideaspace.config

import com.ideaspace.document.documentManagementRoutes
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.plugins.calllogging.CallLogging
import io.ktor.server.plugins.statuspages.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Application.configureRouting() {

    install(CallLogging){

    }

    routing{
        documentManagementRoutes()
    }
    routing {
        get("/") {
            call.respondText("Hello World!")
        }
    }
}
