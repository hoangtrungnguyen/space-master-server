package com.ideaspace.core.repository

import com.ideaspace.core.dao.DocumentDAO
import com.ideaspace.core.kafkaMessage.DocumentSyncEventValue
import com.ideaspace.core.models.BusinessDocument

interface CrudDocumentRepository {
    suspend fun create(request: BusinessDocument): DocumentDAO

    suspend fun findByIdUuid(uuid: String): DocumentDAO

    suspend fun update(id: String, space: Any): Any?

    suspend fun findAll(): List<DocumentDAO>

    suspend fun findById(id: Long): DocumentDAO?

    suspend fun existByUuid(uuid: String): Boolean

    suspend fun existById(id:Long): Boolean

    suspend fun updateOffset(uuid: String, offset: Long)

    suspend fun sendEvent(event: DocumentSyncEventValue)
}