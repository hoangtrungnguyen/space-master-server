package com.ideaspace.document

import com.ideaspace.core.kafkaMessage.EditDocEventValue
import com.ideaspace.core.kafkaMessage.EditElementPayload
import com.ideaspace.core.redis.toRedisDocumentEvent
import com.ideaspace.core.repository.CrudDocumentRepository
import com.ideaspace.core.repository.ElementRepo
import com.ideaspace.core.utils.LogData
import com.ideaspace.workers.DocumentStorage
import com.ideaspace.workers.LogPublisher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.encodeToJsonElement
import kotlin.time.ExperimentalTime

class EditElementCommand(
    val editDocEventValue: EditDocEventValue,
    val docId: Long,
    val processId: Long,
    val documentRedisPublisher: DocumentRedisPublisher,
    val elementRepo: ElementRepo,
    val documentStorage: DocumentStorage,
    val logPublisher: LogPublisher,
    override val documentRepository: CrudDocumentRepository
) : BaseDocCommand {

    @OptIn(ExperimentalTime::class)
    override suspend fun execute() {

        val editElementPayload = editDocEventValue.payload as EditElementPayload

        val elementData = editElementPayload.element
        val document = documentStorage.documentsMap[docId] ?: throw IllegalStateException("Document not found")

        val foundElement = document.searchElement(elementData.uuid)

        if (foundElement == null) {
            logPublisher.warn(
                toLogServer = true, event = LogData(
                    loggerName = this::class.simpleName.toString(),
                    message = Json.encodeToJsonElement(editDocEventValue),
                    userId = editDocEventValue.userId,
                    docId = docId,
                    processId = processId,
                )
            )
            println("⚠️ Element not found")
            return
        }

        val updatedElement = foundElement.copy(
            uuid = elementData.uuid,
            value = elementData.value,
            metadata = elementData.metadata,
            type = elementData.type
        )

        document.update(
            updatedElement,
        )

        val payloadRedis = editDocEventValue.toRedisDocumentEvent()
        val redisEntryId = documentRedisPublisher.publishEditDocEvent(docId, processId, payloadRedis)
        super.saveLatestRedisEntry(docId, redisEntryId)

        println("✅ Updated element ${updatedElement.uuid}")

        withContext(currentCoroutineContext() + Dispatchers.IO) {
            elementRepo.updateEditedElement(
                uuid = elementData.uuid,
                metadata = elementData.metadata,
                value = elementData.value,
                type = elementData.type
            )
        }
    }
}