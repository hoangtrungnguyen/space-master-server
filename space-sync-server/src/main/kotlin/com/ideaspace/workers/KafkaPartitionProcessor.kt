package com.ideaspace.workers

import com.ideaspace.core.dao.DocumentDAO
import com.ideaspace.core.dao.UserDAO
import com.ideaspace.core.kafkaMessage.DocumentSyncEventValue
import com.ideaspace.core.kafkaMessage.SyncOperation
import com.ideaspace.core.repository.CrudDocumentRepository
import com.ideaspace.core.repository.ElementRepo
import com.ideaspace.document.AddElementCommand
import com.ideaspace.document.InitSyncDocument
import io.ktor.server.plugins.di.DependencyRegistry
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.slf4j.LoggerFactory
import java.util.concurrent.ConcurrentHashMap

class KafkaPartitionProcessor(
    val documentRepository: CrudDocumentRepository,
    val redisPublisher: RedisPublisher,
    val elementRepo: ElementRepo,
    val documentStorage: DocumentStorage
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

    suspend fun submit(record: ConsumerRecord<Long, DocumentSyncEventValue>) {

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
            val doc = documentRepository.findById(key)!!

            when (record.value().syncOp) {
                SyncOperation.INIT_SYNC -> initDoc(doc)
                SyncOperation.EDIT_DOC -> editDoc(record, doc)
                SyncOperation.SAVE_DOC -> TODO()
                SyncOperation.FINISH_SYNC -> TODO()
            }
        }

        println("MEMORY WORKER POOL LENGTH: ${partitionScopePool.values.size}")
    }

    suspend fun initDoc(doc: DocumentDAO) {
        InitSyncDocument(
            documentStorage,
            elementRepo
        ).execute(doc)
    }

    private suspend fun editDoc(record: ConsumerRecord<Long, DocumentSyncEventValue>, docDAO: DocumentDAO) {
        println("process record: ${record.value()}")
        val key = record.key()

        if (messageCounter[key] == BATCH_LIMIT) {
            println("IO CONTEXT - Process flush kafka message to database")
            documentRepository.updateOffset(docDAO.uuid.toString(), record.offset())
            messageCounter[key] = 0
        }

        AddElementCommand(
            record, redisPublisher,
            elementRepo,
            documentStorage,
            docDAO
        ).execute()

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