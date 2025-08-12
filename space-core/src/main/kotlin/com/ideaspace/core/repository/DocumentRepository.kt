package com.ideaspace.core.repository

import com.ideaspace.core.datasources.postgres.entities.DocumentDAO
import com.ideaspace.core.repository.dto.CreateDocumentRequest

interface DocumentRepository {
}


interface CrudDocumentRepository : DocumentRepository{
    suspend fun create(request: CreateDocumentRequest): DocumentDAO

    suspend fun findByIdUuid(uuid: String): DocumentDAO

    suspend fun update(id: String, space: Any): Any?

    suspend fun findAll(): List<DocumentDAO>
}