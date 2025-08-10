package com.space

import com.space.features.space.view.routing.spaceRoutes
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.plugins.calllogging.CallLogging
import io.ktor.server.plugins.statuspages.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Application.configureRouting() {

    install(CallLogging){

    }

    install(StatusPages) {
        exception<Throwable> { call, cause ->
            call.respondText(text = "500: $cause", status = HttpStatusCode.InternalServerError)
        }
    }
    routing{
        route("/api"){
            spaceRoutes()
        }
    }
    routing {
        get("/") {
            call.respondText("Hello World!")
        }
    }
}
