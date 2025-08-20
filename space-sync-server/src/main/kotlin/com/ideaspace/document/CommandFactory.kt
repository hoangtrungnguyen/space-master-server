package com.ideaspace.document

import com.ideaspace.core.kafkaMessage.AddElementPayload
import com.ideaspace.core.kafkaMessage.EditDocEventValue
import com.ideaspace.core.kafkaMessage.EditElementPayload
import com.ideaspace.core.kafkaMessage.MoveElementPayload
import com.ideaspace.core.kafkaMessage.RemoveElementPayload
import com.ideaspace.core.models.BusinessDocument
import com.ideaspace.core.repository.CrudDocumentRepository
import com.ideaspace.core.repository.ElementRepo
import com.ideaspace.document.DocumentRedisPublisher
import com.ideaspace.workers.DocumentStorage
import com.ideaspace.workers.LogPublisher


interface BaseDocCommand {
    val documentRepository: CrudDocumentRepository

    suspend fun execute()
    suspend fun saveLatestRedisEntry(
        docId: Long,
        redisEntry: String,
    ) {
        documentRepository.saveLatestRedisEntry(docId, redisEntry)
    }
}

class CommandFactory(
    val documentRedisPublisher: DocumentRedisPublisher,
    val elementRepo: ElementRepo,
    val documentStorage: DocumentStorage,
    val logPublisher: LogPublisher,
    val documentRepository: CrudDocumentRepository
) {

    fun createCommand(
        editDocValue: EditDocEventValue, doc: BusinessDocument, processId: Long
    ): BaseDocCommand {
        return when (editDocValue.payload) {
            is AddElementPayload -> {
                AddElementCommand(
                    editDocValue,
                    doc.id,
                    processId,
                    documentRedisPublisher,
                    elementRepo,
                    documentStorage,
                    logPublisher,
                    documentRepository
                )
            }

            is EditElementPayload -> {
                EditElementCommand(
                    editDocValue,
                    doc.id,
                    processId,
                    documentRedisPublisher,
                    elementRepo,
                    documentStorage,
                    logPublisher,
                    documentRepository
                )
            }

            is MoveElementPayload -> {
                MoveElementCommand(
                    editDocValue,
                    doc.id,
                    processId,
                    documentRedisPublisher,
                    elementRepo,
                    documentStorage,
                    logPublisher,
                    documentRepository
                )
            }

            is RemoveElementPayload -> {
                RemoveElementCommand(
                    editDocValue,
                    doc.id,
                    processId,
                    documentRedisPublisher,
                    elementRepo,
                    documentStorage,
                    logPublisher,
                    documentRepository
                )
            }
        }
    }
}