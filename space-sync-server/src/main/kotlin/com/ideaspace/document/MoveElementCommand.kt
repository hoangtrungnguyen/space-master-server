package com.ideaspace.document

import com.ideaspace.core.kafkaMessage.EditDocEventValue
import com.ideaspace.core.kafkaMessage.MoveElementPayload
import com.ideaspace.core.kafkaMessage.RemoveElementPayload
import com.ideaspace.core.redis.toRedisDocumentEvent
import com.ideaspace.core.repository.ElementRepo
import com.ideaspace.workers.DocumentStorage
import io.lettuce.core.search.arguments.SugAddArgs.Builder.payload
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.withContext
import kotlin.time.ExperimentalTime

// from element to different group or no-group
@OptIn(ExperimentalTime::class)
class MoveElementCommand(
    val editDocEventValue: EditDocEventValue,
    val docId: Long,
    val processId: Long
) {

    suspend fun execute(
        documentRedisPublisher: DocumentRedisPublisher,
        elementRepo: ElementRepo,
        documentStorage: DocumentStorage
    ) : String {
        val removeElementPayload = editDocEventValue.payload as MoveElementPayload
        val element = removeElementPayload.element
        val document = documentStorage.documentsMap[docId]!!

        val prevElementRAM = document.searchElement(element.uuid)!!.copy()

        //remove
        document.remove(prevElementRAM)

        //add
        if(element.parentUuid == null){
            document.addRoot(prevElementRAM.copy(
                parentUuid = null,
            ))
        } else {
            document.addElement( prevElementRAM.copy(
                parentUuid = element.parentUuid!!,
            ))
        }

        //publish changes
        val redisEntryId = documentRedisPublisher.publishEditDocEvent(docId, processId, editDocEventValue.toRedisDocumentEvent())
        return redisEntryId.also {
            println("✅ Moved element ${element.uuid}")
            withContext(currentCoroutineContext() + Dispatchers.IO){
                elementRepo.updateMovedElement(
                    uuid = element.uuid,
                    parentUuid = element.parentUuid
                )
            }
        }
    }
}