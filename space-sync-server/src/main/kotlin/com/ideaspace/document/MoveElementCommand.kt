package com.ideaspace.document

import com.ideaspace.core.kafkaMessage.EditDocEventValue
import com.ideaspace.core.kafkaMessage.MoveElementPayload
import com.ideaspace.core.kafkaMessage.RemoveElementPayload
import com.ideaspace.core.redis.toRedisDocumentEvent
import com.ideaspace.core.repository.ElementRepo
import com.ideaspace.workers.DocumentStorage
import io.lettuce.core.search.arguments.SugAddArgs.Builder.payload
import kotlin.time.ExperimentalTime

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
    ) {
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
        documentRedisPublisher.publishEditDocEvent(docId, processId, editDocEventValue.toRedisDocumentEvent())
        elementRepo.updateMovedElement(
            uuid = element.uuid,
            parentUuid = element.parentUuid
        )
    }
}