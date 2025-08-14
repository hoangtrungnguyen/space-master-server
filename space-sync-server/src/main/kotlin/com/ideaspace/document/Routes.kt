package com.ideaspace.document

import com.ideaspace.core.dto.toDTO
import com.ideaspace.core.repository.CrudDocumentRepository
import io.ktor.server.plugins.di.*
import io.ktor.server.response.*
import io.ktor.server.routing.*


fun Route.documentManagementRoutes() {
    route("/api") {
        route("/documents") {
            get("/{uuid}") {
                val uuid = call.parameters["uuid"]!!
                val response = application.dependencies.resolve<CrudDocumentRepository>().findByIdUuid(uuid)
                val dto = response.toDTO()
                call.respond(dto)
            }

            get("/all"){
                val response = application.dependencies.resolve<CrudDocumentRepository>().findAll()
                val dtos = response.map{ it.toDTO() }.toList()
                call.respond(dtos)
            }
        }
    }
}