package com.ideaspace.document

import io.ktor.http.*
import io.ktor.server.plugins.di.dependencies
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Route.documentManagementRoutes() {
    route("/api") {
        route("/documents") {
            post("/create") {
                val request = call.receive<CreateDocumentRequest>()

                val command = CreateDocumentCommand(request)
                val result = command.execute(application.dependencies)

                call.respond(status = HttpStatusCode.OK, result)
            }

            delete("/{id}") {

            }
        }
    }
}