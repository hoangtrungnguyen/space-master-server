package com.space.com.ideaspace.document

import io.ktor.http.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Route.documentManagementRoutes() {
    route("/api") {
        route("/documents") {
            post("/create") {
                call.respond(status = HttpStatusCode.OK, CreateDocumentCommand().execute())
            }

            delete("/{id}") {

            }
        }
    }
}