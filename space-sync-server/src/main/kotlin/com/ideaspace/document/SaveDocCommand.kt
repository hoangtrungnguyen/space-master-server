package com.ideaspace.document

import com.ideaspace.core.kafkaMessage.SaveDocEventValue
import com.ideaspace.core.models.BusinessDocument

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