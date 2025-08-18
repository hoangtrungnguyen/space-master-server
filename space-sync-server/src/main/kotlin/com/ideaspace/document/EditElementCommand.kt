package com.ideaspace.document

import com.ideaspace.core.kafkaMessage.EditDocEventValue
import com.ideaspace.core.kafkaMessage.EditElementPayload
import com.ideaspace.core.redis.toRedisDocumentEvent
import com.ideaspace.core.repository.ElementRepo
import com.ideaspace.workers.DocumentStorage
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
    ) {

        val editElementPayload = editDocEventValue.payload as EditElementPayload

        val element = editElementPayload.element
        val document = documentStorage.documentsMap[docId]!!

        val prevElement = document.searchElement(element.uuid)!!
        val updatedElement = prevElement.copy(
            uuid = element.uuid,
            value = element.value,
            metadata = element.metadata,
            type = element.type
        )

        document.update(
           updatedElement,
            prevElement.copy()
        )

        val payload = editDocEventValue.toRedisDocumentEvent()
        documentRedisPublisher.publishEditDocEvent(docId, processId, payload)
        elementRepo.updateEditedElement(
            uuid = element.uuid,
            metadata = element.metadata,
            value = element.value,
            type = element.type
        )
    }
}