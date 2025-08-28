package com.ideaspace.document

import com.ideaspace.config.ErrorResponse
import com.ideaspace.core.models.toDTO
import com.ideaspace.core.repository.CrudDocumentRepository
import com.ideaspace.core.repository.ElementRepo
import io.ktor.http.*
import io.ktor.server.plugins.di.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import java.util.*

suspend fun RoutingContext.getDocumentQuery() {
    val rawUuid = call.pathParameters["uuid"] ?: ""
    val uuid: UUID
    try {
        uuid = UUID.fromString(rawUuid)
    } catch (e : IllegalArgumentException) {
        return call.respond(HttpStatusCode.BadRequest, ErrorResponse(
            error = "INVALID_DOCUMENT_UUID",
            message = "Invalid document UUID",
            details = null,
            timestamp = System.currentTimeMillis(),
        ))
    }


    val includes = call.queryParameters["include"]
        ?.split(",")?.map(DocumentInclude::valueOf) ?: emptyList()

    val context = call.application.dependencies.resolve<GetDocumentContext>()

    val doc = context.docRepo.findByUuid(uuid)
        ?: return call.respond(HttpStatusCode.NotFound, ErrorResponse(
            error = "DOCUMENT_NOT_FOUND",
            message = "Document not found",
            details = null,
            timestamp = System.currentTimeMillis()
        ))

    val elements = if (includes.contains(DocumentInclude.E)) {
        context.elementRepo.findAllByDocId(doc.id)
    } else emptyList()

    call.respond(status = HttpStatusCode.OK, doc.toDTO(elements))
}

class GetDocumentContext(
    val docRepo: CrudDocumentRepository,
    val elementRepo: ElementRepo,
)

enum class DocumentInclude {
    // Elements
    E
}