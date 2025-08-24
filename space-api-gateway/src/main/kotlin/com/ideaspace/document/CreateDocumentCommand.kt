package com.ideaspace.document

import com.ideaspace.core.dto.DocumentDTO
import com.ideaspace.core.kafkaMessage.DocumentEventProducer
import com.ideaspace.core.kafkaMessage.InitSyncEventValue
import com.ideaspace.core.kafkaMessage.InitSyncPayload
import com.ideaspace.core.kafkaMessage.SyncOperation
import com.ideaspace.core.models.*
import com.ideaspace.core.repository.CrudDocumentRepository
import com.ideaspace.core.repository.ElementRepo
import io.ktor.server.plugins.di.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonObject
import java.util.*
import kotlin.time.Clock
import kotlin.time.ExperimentalTime
import kotlin.uuid.ExperimentalUuidApi

@OptIn(ExperimentalUuidApi::class, ExperimentalTime::class)
class CreateDocumentCommand(
    val request: CreateDocumentRequest
) {

    suspend fun execute(dependencies: DependencyRegistry): DocumentDTO {
        val docRepo = dependencies.resolve<CrudDocumentRepository>()
        val elementRepo = dependencies.resolve<ElementRepo>()
        val documentEventProducer = dependencies.resolve<DocumentEventProducer>() // Resolve the producer

        val doc = docRepo.create(BusinessDocument(
            id = -1,
            uuid = UUID(0, 0),
            revId = -1,
            title = request.title,
            creatorId = 1,
            ownerId = 1,
            createdAt = Clock.System.now(),
            lastModifiedAt = Clock.System.now(),
            metadata = null,
            documentType = request.documentType,
            status = DocumentStatus.DRAFT,
            transformVersion = 1,
            kafkaOffset = -1,
            latestRedisEntry = ""
        ))

        val root = elementRepo.create(Element(
            uuid = UUID(0, 0),
            docId = doc.id,
            parentUuid = null,
            metadata = null,
            type = "#root",
            value = JsonObject(emptyMap()),
            deletedAt = null
        ))


        val event = InitSyncEventValue(
            syncOp = SyncOperation.INIT_SYNC,
            docId = doc.id,
            processId = request.processId,
            userId = request.userId, // TODO: Get from context
            sessionId = request.sessionId, // TODO: Get from context
            clientId = request.clientId, // TODO: Get from context
            payload = InitSyncPayload()
        )

        withContext(Dispatchers.IO) {
            documentEventProducer.sendEvent(doc.id, event)
        }
        return doc.toDTO(root)
    }

}

@Serializable
data class CreateDocumentRequest(
    val title: String,
    val documentType: DocumentType,
    val processId: Long,
    val userId: Long,
    val sessionId: Long,
    val clientId: Long
)
