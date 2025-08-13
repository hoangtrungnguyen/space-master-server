package com.ideaspace.document

import com.ideaspace.core.models.BusinessDocument
import com.ideaspace.core.models.DocumentStatus
import com.ideaspace.core.models.DocumentType
import com.ideaspace.core.repository.CrudDocumentRepository
import com.ideaspace.core.repository.dto.DocumentDTO
import com.ideaspace.core.repository.dto.toDTO
import io.ktor.server.plugins.di.*
import kotlinx.serialization.Serializable
import kotlin.time.Clock
import kotlin.time.ExperimentalTime
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@OptIn(ExperimentalUuidApi::class, ExperimentalTime::class)
class CreateDocumentCommand(
    val document: CreateDocumentRequest
) {

    suspend fun execute(dependencies: DependencyRegistry): DocumentDTO {
        val docRepo = dependencies.resolve<CrudDocumentRepository>()

        val doc = docRepo.create(BusinessDocument(
            id = -1,
            uuid = Uuid.NIL,
            revId = -1,
            title = document.title,
            creatorId = 1,
            ownerId = 1,
            createdAt = Clock.System.now(),
            lastModifiedAt = Clock.System.now(),
            metadata = null,
            documentType = document.documentType,
            status = DocumentStatus.DRAFT,
            transformVersion = 1,
            kafkaOffset = -1
        ))

        return doc.toDTO()
    }

}

@Serializable
data class CreateDocumentRequest(
    val title: String,
    val documentType: DocumentType,
)
