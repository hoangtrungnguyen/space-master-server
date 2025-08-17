package com.ideaspace.core.repository

import com.ideaspace.core.kafkaMessage.DocumentSyncEventValue
import com.ideaspace.core.models.BusinessDocument

interface CrudDocumentRepository {
    suspend fun create(request: BusinessDocument): BusinessDocument

    suspend fun findByUuid(uuid: String): BusinessDocument?

    suspend fun update(id: String, space: Any): Any?

    suspend fun findAll(): List<BusinessDocument>

    suspend fun findById(id: Long): BusinessDocument?

    suspend fun existByUuid(uuid: String): Boolean

    suspend fun existById(id:Long): Boolean

    suspend fun updateOffset(uuid: String, offset: Long)

    suspend fun sendEvent(event: DocumentSyncEventValue)
}