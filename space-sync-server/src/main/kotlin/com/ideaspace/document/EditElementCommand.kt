package com.ideaspace.document

import com.ideaspace.core.kafkaMessage.EditDocEventValue
import com.ideaspace.core.kafkaMessage.EditElementPayload
import com.ideaspace.core.redis.toRedisDocumentEvent
import com.ideaspace.core.repository.ElementRepo
import com.ideaspace.workers.DocumentStorage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.withContext
import kotlin.time.ExperimentalTime

class EditElementCommand(
    val editDocEventValue: EditDocEventValue,
    val docId: Long,
    val processId: Long
) {

    @OptIn(ExperimentalTime::class)
    suspend fun execute(
        documentRedisPublisher: DocumentRedisPublisher,
        elementRepo: ElementRepo,
        documentStorage: DocumentStorage
    ) : String {

        val editElementPayload = editDocEventValue.payload as EditElementPayload

        val elementData = editElementPayload.element
        val document = documentStorage.documentsMap[docId] ?: throw IllegalStateException("Document not found")

        val foundElement = document.searchElement(elementData.uuid)!!

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
        return documentRedisPublisher.publishEditDocEvent(docId, processId, payloadRedis)
            .also {
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
}