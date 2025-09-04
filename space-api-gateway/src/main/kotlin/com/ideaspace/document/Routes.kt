@file:OptIn(ExperimentalTime::class)

package com.ideaspace.document

import com.ideaspace.config.AuthPrincipal
import com.ideaspace.core.repository.CrudDocumentRepository
import com.ideaspace.core.repository.ElementRepo
import io.ktor.http.*
import io.ktor.server.auth.*
import io.ktor.server.plugins.di.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import kotlin.time.ExperimentalTime

val logger: Logger = LoggerFactory.getLogger("documentManagementRoutes")

fun Route.documentManagementRoutes() {

    application.dependencies.provide<GetDocumentContext> {
        val docRepo = resolve<CrudDocumentRepository>()
        val elementRepo = resolve<ElementRepo>()
        GetDocumentContext(docRepo, elementRepo)
    }

    authenticate("auth-session") {

        post("/api/documents/create") {
            val request = call.receive<CreateDocumentRequest>()
            val principal = call.principal<AuthPrincipal>()!!

            val command = CreateDocumentCommand(principal, request)
            val result = command.execute(application.dependencies)

            call.respond(status = HttpStatusCode.OK, result)
        }

        delete("/api/documents/{id}") {

        }

        get("/api/documents") {
            val principal = call.principal<AuthPrincipal>()!!
            val user = principal.user

            val command = GetAllDocument(
                user.id,
                application.dependencies.resolve()
            )
            val result = command.execute()
            call.respond(status = HttpStatusCode.OK, result)
        }

        get("/api/documents/{uuid}", RoutingContext::getDocumentQuery)
    }

}
