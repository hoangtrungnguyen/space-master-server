package com.ideaspace.document

import com.ideaspace.core.dto.DocumentDashboardItemDTO
import com.ideaspace.core.repository.CrudDocumentRepository
import kotlin.time.ExperimentalTime

@OptIn(ExperimentalTime::class)
class GetAllDocument(
    val ownerId: Long,
    val documentRepo: CrudDocumentRepository
) {
    suspend fun execute(): List<DocumentDashboardItemDTO> {
        val data = documentRepo.findAllByOwnerId(ownerId)
        return data.map {
            DocumentDashboardItemDTO(
                it.uuid,
                it.title,
                it.createdAt
            )
        }.toList()
    }
}