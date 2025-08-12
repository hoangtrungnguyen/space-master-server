package com.ideaspace.core.repositoryImpl

import kotlinx.coroutines.runBlocking
import com.ideaspace.core.datasources.postgres.entities.DocumentDAO
import com.ideaspace.core.datasources.postgres.entities.DocumentTable
import com.ideaspace.core.repository.CrudDocumentRepository
import com.ideaspace.core.repository.dto.CreateDocumentRequest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import org.jetbrains.exposed.v1.core.SqlExpressionBuilder.eq
import org.jetbrains.exposed.v1.jdbc.SchemaUtils
import org.jetbrains.exposed.v1.jdbc.transactions.transaction
import java.util.UUID

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
        TODO("Not yet implemented")
    }

    override suspend fun findAll(): List<DocumentDAO> = transaction{
        DocumentDAO.all().toList()
    }
}