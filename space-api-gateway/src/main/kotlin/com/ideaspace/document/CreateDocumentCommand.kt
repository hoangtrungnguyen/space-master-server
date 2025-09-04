package com.ideaspace.document

import com.ideaspace.config.AuthPrincipal
import com.ideaspace.core.dto.DocumentDTO
import com.ideaspace.core.models.BusinessDocument
import com.ideaspace.core.models.DocumentStatus
import com.ideaspace.core.models.DocumentType
import com.ideaspace.core.models.toDTO
import com.ideaspace.core.repository.CrudDocumentRepository
import io.ktor.server.plugins.di.*
import kotlinx.serialization.Serializable
import java.util.*
import kotlin.time.Clock
import kotlin.time.ExperimentalTime
import kotlin.uuid.ExperimentalUuidApi

@OptIn(ExperimentalUuidApi::class, ExperimentalTime::class)
class CreateDocumentCommand(
    val principal: AuthPrincipal,
    val document: CreateDocumentRequest
) {

    suspend fun execute(dependencies: DependencyRegistry): DocumentDTO {
        val docRepo = dependencies.resolve<CrudDocumentRepository>()

        val doc = docRepo.create(BusinessDocument(
            id = -1,
            uuid = UUID(0, 0),
            revId = -1,
            title = document.title,
            creatorId = principal.user.id,
            ownerId = principal.user.id,
            createdAt = Clock.System.now(),
            lastModifiedAt = Clock.System.now(),
            metadata = null,
            documentType = document.documentType,
            status = DocumentStatus.DRAFT,
            transformVersion = 1,
            kafkaOffset = -1,
            latestRedisEntry = ""
        ))

        return doc.toDTO(listOf())
    }

}

@Serializable
data class CreateDocumentRequest(
    val title: String,
    val documentType: DocumentType,
)
