package com.ideaspace.core.repository

import com.ideaspace.core.dao.DocumentDAO
import com.ideaspace.core.dto.CreateDocumentRequest

interface DocumentRepository {
}


interface CrudDocumentRepository : DocumentRepository{
    suspend fun create(request: CreateDocumentRequest): DocumentDAO

    suspend fun findByIdUuid(uuid: String): DocumentDAO

    suspend fun update(id: String, space: Any): Any?

    suspend fun findAll(): List<DocumentDAO>

    suspend fun existByUuid(uuid: String): Boolean

    suspend fun updateOffset(uuid: String, offset: Int)
}