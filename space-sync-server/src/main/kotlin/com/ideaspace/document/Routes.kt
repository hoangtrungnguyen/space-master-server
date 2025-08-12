package com.ideaspace.document

import io.ktor.http.HttpStatusCode
import io.ktor.server.application.Application
import io.ktor.server.plugins.di.dependencies
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.application
import io.ktor.server.routing.delete
import io.ktor.server.routing.post
import io.ktor.server.routing.route
import com.ideaspace.core.repository.CrudDocumentRepository
import com.ideaspace.core.repository.dto.CreateDocumentRequest


fun Route.documentManagementRoutes() {
    route("/api") {
        route("/documents") {
            post("/create") {
                val response = application.dependencies.resolve<CrudDocumentRepository>().create(
                    CreateDocumentRequest(
                        name = "test"
                    )
                )
                call.respond(HttpStatusCode.OK, response.toString())
            }

            delete("/{id}") {

            }
        }
    }
}