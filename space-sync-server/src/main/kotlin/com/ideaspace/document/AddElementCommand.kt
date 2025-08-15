package com.ideaspace.document

import com.ideaspace.core.kafkaMessage.DocumentSyncEventValue
import com.ideaspace.core.models.Element
import com.ideaspace.core.redis.toRedis
import com.ideaspace.core.repository.ElementRepo
import com.ideaspace.workers.RedisPublisher
import kotlinx.serialization.json.JsonObject
import org.apache.avro.generic.GenericRecord
import org.apache.kafka.clients.consumer.ConsumerRecord
import java.util.LinkedHashMap
import java.util.UUID
import javax.sql.rowset.WebRowSet
import kotlin.time.ExperimentalTime

class AddElementCommand(
    val consumerRecord: ConsumerRecord<Long, DocumentSyncEventValue>,
    val redisPublisher: RedisPublisher,
    val elementRepo: ElementRepo,
) {

    @OptIn(ExperimentalTime::class)
    suspend fun execute(){
        val document =  consumerRecord.value()
        val element = document.payload.element
        //TODO
//        redisPublisher.publishDocumentEvent(document.toRedis())
        elementRepo.insert(
            Element(
                uuid = element.uuid,
                docId = document.docId,
                parentUuid = element.parentUuid,
                metadata = element.metadata,
                type = element.type,
                value = element.value,
                deletedAt = null
            )
        )
    }
}