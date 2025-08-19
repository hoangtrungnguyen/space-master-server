package com.ideaspace.document

import com.ideaspace.core.kafkaMessage.EditDocEventValue
import com.ideaspace.core.kafkaMessage.EditDocPayload
import com.ideaspace.core.kafkaMessage.RemoveElementPayload
import com.ideaspace.core.models.Element
import com.ideaspace.core.redis.toRedisDocumentEvent
import com.ideaspace.core.repository.ElementRepo
import com.ideaspace.workers.DocumentStorage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.withContext

class RemoveElementCommand(
    val editDocEventValue: EditDocEventValue,
    val docId: Long,
    val processId: Long

){
    suspend fun execute(
        documentRedisPublisher: DocumentRedisPublisher,
        elementRepo: ElementRepo,
        documentStorage: DocumentStorage
    ) : String{
        val removeElementPayload = editDocEventValue.payload as RemoveElementPayload
        val element = removeElementPayload.element
        val document = documentStorage.documentsMap[docId]!!
        val prevElementRAM = document.searchElement(element.uuid) ?: throw Exception("Not found ${element.uuid}")

        document.remove(prevElementRAM)

        val redisEntryId = documentRedisPublisher.publishEditDocEvent(
            docId,
            processId,
            editDocEventValue.toRedisDocumentEvent()
        )

        return redisEntryId.also {
            println("✅ Remove element ${element.uuid}")
            withContext(currentCoroutineContext() + Dispatchers.IO){
                    elementRepo.deleteByUuid(
                        element.uuid
                    )
            }
        }
    }
}