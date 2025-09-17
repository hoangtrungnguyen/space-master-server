package com.ideaspace.core.repositoryImpl

import com.ideaspace.core.dao.DocumentDAO
import com.ideaspace.core.dao.DocumentTable
import com.ideaspace.core.dao.toModel
import com.ideaspace.core.models.BusinessDocument
import com.ideaspace.core.repository.CrudDocumentRepository
import kotlinx.coroutines.runBlocking
import org.jetbrains.exposed.v1.core.SortOrder
import org.jetbrains.exposed.v1.jdbc.Database
import org.jetbrains.exposed.v1.jdbc.SchemaUtils
import org.jetbrains.exposed.v1.jdbc.transactions.transaction
import org.jetbrains.exposed.v1.jdbc.update
import java.util.*
import kotlin.time.ExperimentalTime

class CrudDocumentRepositoryImpl(val db: Database) : CrudDocumentRepository {

    init {
        runBlocking {
            transaction(db) {
                SchemaUtils.create(DocumentTable)

                val missingColStatements = SchemaUtils.addMissingColumnsStatements(
                    DocumentTable,
                    withLogs = true
                )

                missingColStatements.forEach {
                    exec(it)
                }
            }
        }
    }

    override suspend fun create(request: BusinessDocument): BusinessDocument = transaction(db) {
        val generated = DocumentDAO.new {
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
        generated.toModel()
    }

    override suspend fun findByUuid(uuid: String): BusinessDocument? = transaction(db) {
        DocumentDAO.find { DocumentTable.uuid eq UUID.fromString(uuid) }.firstOrNull()?.toModel()
    }

    override suspend fun findByUuid(uuid: UUID): BusinessDocument? = transaction(db) {
        DocumentDAO.find { DocumentTable.uuid eq uuid }.firstOrNull()?.toModel()
    }

    @OptIn(ExperimentalTime::class)
    override suspend fun findAll(): List<BusinessDocument> = transaction(db) {
        DocumentDAO.all().orderBy(
            DocumentTable.createdAt to SortOrder.DESC
        ).map { it.toModel() }.toList()
    }

    override suspend fun findById(id: Long): BusinessDocument? = transaction(db) {
        DocumentDAO.findById(id)?.toModel()
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
            DocumentTable.update({ DocumentTable.uuid eq parsedUuid }) {
                it[kafkaOffset] = offset
            }
        }
    }

    override suspend fun saveLatestRedisEntry(id: Long, entryId: String) {
        transaction(db) {
            DocumentTable.update({ DocumentTable.id eq id }) {
                it[latestRedisEntry] = entryId
            }
        }
    }

    @OptIn(ExperimentalTime::class)
    override suspend fun findAllByOwnerId(ownerId: Long): List<BusinessDocument> = transaction(db) {
        DocumentDAO.all().orderBy(DocumentTable.createdAt to SortOrder.DESC).filter {
            it.ownerId == ownerId
        }.map { it.toModel() }.toList()
    }
}