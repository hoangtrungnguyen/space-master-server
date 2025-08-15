package com.ideaspace.workers

import com.ideaspace.core.kafkaMessage.DocumentSyncEventValue
import com.ideaspace.core.repository.CrudDocumentRepository
import com.ideaspace.core.repository.ElementRepo
import com.ideaspace.document.AddElementCommand
import io.ktor.server.plugins.di.DependencyRegistry
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.slf4j.LoggerFactory
import java.util.concurrent.ConcurrentHashMap

class KafkaPartitionProcessor {
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

    suspend fun submit(registry: DependencyRegistry, record: ConsumerRecord<Long, DocumentSyncEventValue>) {
        val documentRepository = registry.resolve<CrudDocumentRepository>()
        val redisPublisher = registry.resolve<RedisPublisher>()
        val elementRepo = registry.resolve<ElementRepo>()
        kafkaLogger.info("[SUBMIT TO WORKER] Received record ${record.value()} from topic ${record.topic()}")

        val key = record.key()
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

            AddElementCommand(
                record, redisPublisher,
                elementRepo
            ).execute()

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