package com.ideaspace.document

import com.ideaspace.core.kafkaMessage.FinishSyncEventValue
import com.ideaspace.core.kafkaMessage.FinishSyncPayload
import com.ideaspace.core.models.BusinessDocument
import com.ideaspace.core.redis.RedisFinishSyncEvent
import com.ideaspace.core.redis.toRedisDocumentEvent
import com.ideaspace.core.repository.ElementRepo
import com.ideaspace.workers.DocumentStorage

class FinishedSyncDocCommand(
    val doc: BusinessDocument,
    val finishSyncEventValue: FinishSyncEventValue
    ) {

    suspend fun execute(
        documentPublisher: DocumentRedisPublisher,
        documentStorage: DocumentStorage,
        elementRepo: ElementRepo
    ){
        documentPublisher.publishFinishSyncDocEvent(
            finishSyncEventValue.toRedisDocumentEvent() as RedisFinishSyncEvent
        )
    }
}