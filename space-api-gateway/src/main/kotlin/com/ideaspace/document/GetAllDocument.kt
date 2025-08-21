package com.ideaspace.document

import com.ideaspace.core.dto.DocumentDashboardItemDTO
import com.ideaspace.core.repository.CrudDocumentRepository
import kotlin.time.ExperimentalTime

@OptIn(ExperimentalTime::class)
class GetAllDocument(
    val userId: Long,
    val documentRepo: CrudDocumentRepository
) {
    suspend fun execute(): List<DocumentDashboardItemDTO> {
        val data = documentRepo.findAll()

        return data.map {
            DocumentDashboardItemDTO(
                it.uuid,
                it.title,
                it.createdAt
            )
        }.toList()

    }
}