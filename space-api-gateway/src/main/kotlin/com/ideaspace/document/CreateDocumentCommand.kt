package com.ideaspace.document

import io.ktor.server.plugins.di.DependencyRegistry
import kotlinx.serialization.Serializable

class CreateDocumentCommand(
    val document: CreateDocumentRequest
) {

    fun execute(dependencies: DependencyRegistry): CreateDocumentResponse {
        println("received: $document")
        return CreateDocumentResponse(
            message = "OK",
            received = document
        )
    }

}

@Serializable
data class CreateDocumentRequest(
    val title: String
)

@Serializable
data class CreateDocumentResponse(
    val message: String,
    val received: CreateDocumentRequest
)