package com.ideaspace.workers

import com.ideaspace.core.repository.CrudDocumentRepository
import io.github.flaxoos.ktor.server.plugins.kafka.KafkaRecordKey
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.apache.avro.generic.GenericRecord
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.slf4j.LoggerFactory
import java.util.concurrent.ConcurrentHashMap

class KafkaPartitionProcessor(
    val documentRepository: CrudDocumentRepository
) {
    companion object {
        var count = 0
    }

    init {
        count++
        println("KafkaPartitionProcessor ${count}")
    }
    private val BATCH_LIMIT = 10

    private val partitionScopePool = ConcurrentHashMap<String, CoroutineScope>()
    private val messageCounter = ConcurrentHashMap<String, Int>()
    private val kafkaLogger = LoggerFactory.getLogger("kafka-consumer")

    fun submit(record: ConsumerRecord<KafkaRecordKey, GenericRecord>) {
        kafkaLogger.info("[SUBMIT TO WORKER] Received record ${record.value()} from topic ${record.topic()}")
        val partition = record.partition()
        val key = record.key()
        println("key ${key}")
        val partitionScope =
            partitionScopePool.computeIfAbsent(key) { CoroutineScope(SupervisorJob() + Dispatchers.IO) }

        val counter = messageCounter.computeIfAbsent(key){
            0
        }

        messageCounter[key] = counter + 1
        println("KEY ${key} - MESSAGE COUNTER: ${messageCounter[key]}")
        partitionScope.launch {
            println("process record: ${record.value()}")
            if(messageCounter[key] == BATCH_LIMIT){
                    println("IO CONTEXT - Process flush kafka message to database")
                    documentRepository.updateOffset(key, record.offset().toInt())
                messageCounter[key] = 0
            }
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