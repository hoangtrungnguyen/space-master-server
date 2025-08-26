package com.ideaspace.document

import com.ideaspace.core.kafkaMessage.SaveDocEventValue
import com.ideaspace.core.models.BusinessDocument
import com.ideaspace.core.redis.RedisSaveDocEvent
import com.ideaspace.core.redis.toRedisDocumentEvent
import com.ideaspace.core.repository.ElementRepo
import com.ideaspace.workers.DocumentStorage

class SaveDocCommand(
    val doc: BusinessDocument,
    val saveDocEventValue: SaveDocEventValue
) {

    suspend fun execute(
        documentPublisher: DocumentRedisPublisher,
    ) {
        documentPublisher.publishSaveDocEvent(saveDocEventValue)
    }
}