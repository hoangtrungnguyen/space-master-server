package com.ideaspace.document

import com.ideaspace.core.kafkaMessage.EditDocEventValue
import com.ideaspace.core.kafkaMessage.EditDocPayload
import com.ideaspace.core.kafkaMessage.RemoveElementPayload
import com.ideaspace.core.redis.toRedisDocumentEvent
import com.ideaspace.core.repository.ElementRepo
import com.ideaspace.workers.DocumentStorage

class RemoveElementCommand(
    val editDocEventValue: EditDocEventValue,
    val docId: Long,
    val processId: Long

){
    suspend fun execute(
        documentRedisPublisher: DocumentRedisPublisher,
        elementRepo: ElementRepo,
        documentStorage: DocumentStorage
    ) {
        val removeElementPayload = editDocEventValue.payload as RemoveElementPayload
        val element = removeElementPayload.element
        val document = documentStorage.documentsMap[docId]!!
        val prevElement = document.searchElement(element.uuid)!!

        document.remove(prevElement)

        documentRedisPublisher.publishEditDocEvent(
            docId,
            processId,
            editDocEventValue.toRedisDocumentEvent()
        )

        // DB
        elementRepo.deleteByUuid(
            element.uuid
        )
    }
}