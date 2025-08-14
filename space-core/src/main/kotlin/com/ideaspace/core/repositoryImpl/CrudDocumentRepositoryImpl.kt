package com.ideaspace.core.repositoryImpl

import com.ideaspace.core.dao.DocumentDAO
import com.ideaspace.core.dao.DocumentTable
import com.ideaspace.core.kafkaMessage.DocumentSyncEventValue
import com.ideaspace.core.models.BusinessDocument
import com.ideaspace.core.repository.CrudDocumentRepository
import kotlinx.coroutines.runBlocking
import org.jetbrains.exposed.v1.jdbc.Database
import org.jetbrains.exposed.v1.jdbc.SchemaUtils
import org.jetbrains.exposed.v1.jdbc.transactions.transaction
import java.util.*
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

class CrudDocumentRepositoryImpl(val db: Database ) : CrudDocumentRepository {

    init {
        runBlocking {
            transaction(db) {
                SchemaUtils.create(DocumentTable)
            }
        }
    }

    override suspend fun create(request: BusinessDocument): DocumentDAO = transaction(db) {
        DocumentDAO.new {
            revId = 1       // TODO Generate
            title = request.title
            creatorId = request.creatorId
            ownerId = request.ownerId
            metadata = request.metadata
            documentType = request.documentType
            status = request.status
            transformVersion = request.transformVersion
            kafkaOffset = request.kafkaOffset
        }
    }

    override suspend fun findByIdUuid(uuid: String): DocumentDAO = transaction(db) {
        DocumentDAO.find { DocumentTable.uuid eq UUID.fromString(uuid) }.first()
    }

    override suspend fun update(id: String, space: Any): Any? {
        TODO()
    }

    override suspend fun findAll(): List<DocumentDAO> = transaction(db) {
        DocumentDAO.all().toList()
    }

    override suspend fun findById(id: Long): DocumentDAO? = transaction(db){
        DocumentDAO.findById(id)
    }

    override suspend fun existByUuid(uuid: String): Boolean {
        val parsedUuid = runCatching { UUID.fromString(uuid) }.getOrNull() ?: return false
        return transaction(db) {
            !DocumentDAO.find { DocumentTable.uuid eq parsedUuid }.empty()
        }
    }

    override suspend fun existById(id: Long): Boolean {
        return transaction(db) {
            DocumentDAO.findById(id) != null
        }
    }

    @OptIn(ExperimentalTime::class)
    override suspend fun updateOffset(uuid: String, offset: Long) {
        val parsedUuid = runCatching { UUID.fromString(uuid) }.getOrNull() ?: return
        transaction(db) {
            DocumentDAO.find { DocumentTable.uuid eq parsedUuid }.firstOrNull()?.also {
                it.kafkaOffset = offset
                it.lastModifiedAt = Clock.System.now()
            }
        }
    }

    override suspend fun sendEvent(event: DocumentSyncEventValue) {
        TODO("Not yet implemented")
    }
}