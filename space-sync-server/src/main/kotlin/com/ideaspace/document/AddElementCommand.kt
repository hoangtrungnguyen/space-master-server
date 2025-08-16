package com.ideaspace.document

import com.ideaspace.core.dao.DocumentDAO
import com.ideaspace.core.kafkaMessage.DocumentSyncEventValue
import com.ideaspace.core.models.Element
import com.ideaspace.core.ram.ElementRAM
import com.ideaspace.core.redis.toRedis
import com.ideaspace.core.repository.ElementRepo
import com.ideaspace.workers.DocumentStorage
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
    val documentStorage: DocumentStorage,
    val docDao: DocumentDAO
) {

    @OptIn(ExperimentalTime::class)
    suspend fun execute() {
        val documentEvent = consumerRecord.value()
        val element = documentEvent.payload.element
        //TODO
//        redisPublisher.publishDocumentEvent(document.toRedis())
        val document = documentStorage.documentsMap[docDao.id.value]!!
        if (element.parentUuid == null) {
            document.addRoot(
                element.uuid, ElementRAM(
                    uuid = element.uuid,
                    element = null,
                    value = element.value,
                    metadata = element.metadata,
                    path = element.uuid.toString(),
                    children = LinkedHashMap(),
                    deletedAt = null
                )
            )
        } else {
            val root = documentStorage.documentsMap[docDao.id.value]!!.roots[element.parentUuid]!!
            document.addElement(
                root, element.parentUuid!!, ElementRAM(
                    uuid = element.uuid,
                    element = null,
                    value = element.value,
                    metadata = element.metadata,
                    path = "",
                    children = LinkedHashMap(),
                    deletedAt = null
                )
            )
        }
        elementRepo.insert(
            Element(
                uuid = element.uuid,
                docId = docDao.id.value,
                parentUuid = element.parentUuid,
                metadata = element.metadata,
                type = element.type,
                value = element.value,
                deletedAt = null
            )
        )
    }
}