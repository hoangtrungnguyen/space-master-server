package com.ideaspace.document

import com.ideaspace.core.kafkaMessage.*
import com.ideaspace.core.models.BusinessDocument
import com.ideaspace.core.repository.CrudDocumentRepository
import com.ideaspace.core.repository.ElementRepo
import com.ideaspace.workers.DocumentStorage
import com.ideaspace.workers.LogPublisher
import kotlin.reflect.typeOf


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
        editDocValue: DocumentSyncEventValue, doc: BusinessDocument, processId: Long
    ): BaseDocCommand {
        return when (editDocValue) {
            is AddElementEventValue -> {
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

            is EditElementEventValue -> {
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

            is MoveElementEventValue -> {
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

            is RemoveElementEventValue -> {
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
            else -> throw RuntimeException("Missing handle for ${editDocValue.javaClass.simpleName}")
        }
    }
}