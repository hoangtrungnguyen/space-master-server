package com.ideaspace.document

import com.ideaspace.core.dto.toDTO
import com.ideaspace.core.repository.CrudDocumentRepository
import com.ideaspace.workers.DocumentStorage
import io.ktor.server.plugins.di.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Route.documentManagementRoutes() {
    route("/api") {
        route("/documents") {
            get("/all"){
                val response = application.dependencies.resolve<CrudDocumentRepository>().findAll()
                val dtos = response.map{ it.toDTO() }.toList()
                call.respond(dtos)
            }
        }
        get("/storage"){
            val response = application.dependencies.resolve<DocumentStorage>().documentsMap
            call.respondText(response.toString())
        }
    }
}