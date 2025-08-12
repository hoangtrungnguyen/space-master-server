package com.ideaspace.document

import io.ktor.server.plugins.di.DependencyRegistry
import kotlinx.serialization.Serializable
import kotlin.random.Random

class CreateDocumentCommand(
    val document: CreateDocumentRequest
) {

    suspend fun execute(dependencies: DependencyRegistry): CreateDocumentResponse {
        
        val producer = dependencies.resolve<DocumentEventProducer>()

        val documentId = Random.nextLong()
        val documentEvent = DocumentEvent(
            op = "CREATE",
        )
        
        producer.sendEvent(key = documentId, event = documentEvent)
        
        return CreateDocumentResponse(
            documentId = documentId
        )
    }

}

@Serializable
data class CreateDocumentRequest(
    val title: String
)

@Serializable
data class CreateDocumentResponse(
    val documentId: Long,
)