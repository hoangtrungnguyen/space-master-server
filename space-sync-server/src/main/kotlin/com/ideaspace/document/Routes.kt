package com.ideaspace.document

import com.ideaspace.core.dto.toDTO
import com.ideaspace.core.repository.CrudDocumentRepository
import com.ideaspace.workers.DocumentStorage
import io.ktor.http.HttpStatusCode
import io.ktor.server.plugins.di.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Route.documentManagementRoutes() {
    route("/api") {
        route("/documents") {
            get("/all"){
                val response = application.dependencies.resolve<CrudDocumentRepository>().findAll()
                call.respondText(status= HttpStatusCode.OK,text= response.toString())
            }
            get("/{uuid}"){
                val documentId = call.parameters["uuid"]?.toLong() ?: throw IllegalArgumentException("Document ID is required")
                val document = application.dependencies.resolve<CrudDocumentRepository>().findById(documentId) ?: throw NoSuchElementException("Document not found")
                val root = application.dependencies.resolve<DocumentStorage>().documentsMap[document.id]?.roots?.values?.map {
                    it.toString()
                } ?: throw NoSuchElementException("Document content not found in storage")
                call.respondText(text = root.toString(), status = HttpStatusCode.OK)
            }
        }

        get("/storage"){
            val response = application.dependencies.resolve<DocumentStorage>().documentsMap
            call.respondText(response.toString())
        }

    }
}