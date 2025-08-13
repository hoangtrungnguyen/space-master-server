package com.ideaspace.user

import io.ktor.http.HttpStatusCode
import io.ktor.server.plugins.di.dependencies
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.application
import io.ktor.server.routing.post
import io.ktor.server.routing.route

fun Route.userManagementRoutes() {
    route("/api") {
        route("/users") {
            post("/register") {
                val request = call.receive<RegisterNameRequest>()
                val command = RegisterNameCommand(request)
                val data = command.execute(application.dependencies)
                call.respond(HttpStatusCode.OK,data )
            }
        }
    }

}
