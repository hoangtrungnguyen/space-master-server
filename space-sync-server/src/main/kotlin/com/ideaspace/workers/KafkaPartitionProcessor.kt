package com.ideaspace.workers

import com.ideaspace.core.redis.RedisSyncOperation
import com.ideaspace.core.repository.CrudDocumentRepository
import io.github.flaxoos.ktor.server.plugins.kafka.KafkaRecordKey
import io.swagger.v3.oas.annotations.links.Link
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.encodeToJsonElement
import org.apache.avro.generic.GenericRecord
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.slf4j.LoggerFactory
import java.util.LinkedHashMap
import java.util.concurrent.ConcurrentHashMap

class KafkaPartitionProcessor(
    val documentRepository: CrudDocumentRepository,
    val redisPublisher: RedisPublisher
) {
    companion object {
        var count = 0
    }

    init {
        count++
        println("KafkaPartitionProcessor ${count}")
    }

    private val BATCH_LIMIT = 10

    private val partitionScopePool = ConcurrentHashMap<Long, CoroutineScope>()
    private val messageCounter = ConcurrentHashMap<Long, Int>()
    private val kafkaLogger = LoggerFactory.getLogger("kafka-consumer")

    fun submit(record: ConsumerRecord<Long, GenericRecord>) {
        kafkaLogger.info("[SUBMIT TO WORKER] Received record ${record.value()} from topic ${record.topic()}")
        val partition = record.partition()
        val value = record.value() as LinkedHashMap<*, *>

        kafkaLogger.info("Kafka message value: $value")

        val key = record.key()
        println("key ${key}")
        val partitionScope =
            partitionScopePool.computeIfAbsent(key) { CoroutineScope(SupervisorJob() + Dispatchers.IO) }

        val counter = messageCounter.computeIfAbsent(key) {
            0
        }

        messageCounter[key] = counter + 1
        println("KEY ${key} - MESSAGE COUNTER: ${messageCounter[key]}")
        partitionScope.launch {
            println("process record: ${record.value()}")

            if (messageCounter[key] == BATCH_LIMIT) {
                println("IO CONTEXT - Process flush kafka message to database")
                val doc = documentRepository.findById(key)!!
                documentRepository.updateOffset(doc.uuid.toString(), record.offset())
                messageCounter[key] = 0
            }


            val payload = value["payload"] as LinkedHashMap<*, *>

            val element = payload["element"] as LinkedHashMap<*, *>

            val elementMetadata = element["metadata"] as LinkedHashMap<*, *>
            val elementValue = element["value"] as LinkedHashMap<*, *>

            redisPublisher.saveSyncOperation(
                RedisSyncOperation(
                    syncOp = value["sync_op"].toString(),
                    docId = value["doc_id"].toString().toLong(),
                    processId = value["process_id"].toString().toLong(),
                    userId = value["user_id"].toString().toInt(),
                    sessionId = value["session_id"].toString().toLong(),
                    clientId = value["client_id"].toString().toInt(),
                    payload = com.ideaspace.core.redis.Payload(
                        elementOp = payload["element_op"].toString(),
                        element = com.ideaspace.core.redis.Element(
                            uuid = element["uuid"].toString(),
                            parentUuid = element["parent_uuid"].toString(),
                            metadata = if (elementMetadata.isEmpty()) JsonObject(emptyMap()) else Json.encodeToJsonElement(
                                elementMetadata
                            ),
                            type = element["type"].toString(),
                            value = if (elementValue.isEmpty()) JsonObject(emptyMap()) else Json.encodeToJsonElement(
                                elementValue
                            ),

                        )
                    )
                )
            )


        }

        println("MEMORY WORKER POOL LENGTH: ${partitionScopePool.values.size}")
    }

    fun shutdown() {
        println("Shutting down all partition processors by cancelling the scope...")
        partitionScopePool.values.forEach {
            it.cancel()
        }
        partitionScopePool.clear()
        println("All partition processors shut down.")
    }
}