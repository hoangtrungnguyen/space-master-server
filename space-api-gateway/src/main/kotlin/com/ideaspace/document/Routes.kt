@file:OptIn(ExperimentalTime::class)

package com.ideaspace.document

import com.ideaspace.config.AuthPrincipal
import com.ideaspace.core.models.BusinessDocument
import io.ktor.http.*
import io.ktor.server.auth.*
import io.ktor.server.plugins.di.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlin.time.ExperimentalTime

fun Route.documentManagementRoutes() {
    authenticate("auth-session") {

        post("/api/documents/create") {
            val request = call.receive<CreateDocumentRequest>()

            val command = CreateDocumentCommand(request)
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

        get("/api/documents/{uuid}") {

        }
    }

}

/**
 * Validates that a user has permission to access a specific document
 */
fun validateDocumentAccess(authPrincipal: AuthPrincipal, document: BusinessDocument): Boolean {

    // TODO: Implement proper authorization logic
    // Examples of checks you might want to add:
    // - Is the user the owner of the document?
    // - Is the user a member of the team/workspace that owns the document?
    // - Does the user have specific permissions (read/write) for this document?
    // - Is the document public or private?

    // For now, return true if document exists and user is authenticated
    // In production, implement your specific business logic here

    println("Document access granted for user ${authPrincipal.user.loginName} to document")

    return true
}