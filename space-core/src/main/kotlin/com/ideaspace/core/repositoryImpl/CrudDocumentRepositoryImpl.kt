package com.ideaspace.core.repositoryImpl

import kotlinx.coroutines.runBlocking
import com.ideaspace.core.dao.DocumentDAO
import com.ideaspace.core.dao.DocumentTable
import com.ideaspace.core.repository.CrudDocumentRepository
import com.ideaspace.core.dto.CreateDocumentRequest
import org.jetbrains.exposed.v1.jdbc.SchemaUtils
import org.jetbrains.exposed.v1.jdbc.transactions.transaction
import java.util.UUID
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

class CrudDocumentRepositoryImpl(
) : CrudDocumentRepository {

    init {
        runBlocking {
            transaction {
                SchemaUtils.create(DocumentTable)
            }
        }
    }

    override suspend fun create(request: CreateDocumentRequest): DocumentDAO = transaction {
        DocumentDAO.new {
            title = request.name
        }
    }

    override suspend fun findByIdUuid(uuid: String): DocumentDAO = transaction {
        DocumentDAO.find { DocumentTable.uuid eq UUID.fromString(uuid) }.first()
    }

    override suspend fun update(id: String, space: Any): Any? {
        TODO()
    }

    override suspend fun findAll(): List<DocumentDAO> = transaction{
        DocumentDAO.all().toList()
    }

    override suspend fun existByUuid(uuid: String): Boolean {
        val parsedUuid = runCatching { UUID.fromString(uuid) }.getOrNull() ?: return false
        return transaction {
            !DocumentDAO.find { DocumentTable.uuid eq parsedUuid }.empty()
        }
    }

    @OptIn(ExperimentalTime::class)
    override suspend fun updateOffset(uuid: String, offset: Int) {
        val parsedUuid = runCatching { UUID.fromString(uuid) }.getOrNull() ?: return
        transaction {
            DocumentDAO.find { DocumentTable.uuid eq parsedUuid }.firstOrNull()?.also {
                it.kafkaOffset = offset
                it.updatedAt = Clock.System.now()
            }
        }
    }
}