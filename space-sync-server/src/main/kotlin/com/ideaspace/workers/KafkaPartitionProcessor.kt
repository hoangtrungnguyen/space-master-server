package com.ideaspace.workers

import com.ideaspace.core.kafkaMessage.AddElementPayload
import com.ideaspace.core.kafkaMessage.DocumentSyncEventValue
import com.ideaspace.core.kafkaMessage.EditDocEventValue
import com.ideaspace.core.kafkaMessage.EditElementPayload
import com.ideaspace.core.kafkaMessage.FinishSyncEventValue
import com.ideaspace.core.kafkaMessage.InitSyncEventValue
import com.ideaspace.core.kafkaMessage.MoveElementPayload
import com.ideaspace.core.kafkaMessage.RemoveElementPayload
import com.ideaspace.core.kafkaMessage.SaveDocEventValue
import com.ideaspace.core.models.BusinessDocument
import com.ideaspace.core.repository.CrudDocumentRepository
import com.ideaspace.core.repository.ElementRepo
import com.ideaspace.document.AddElementCommand
import com.ideaspace.document.CommandFactory
import com.ideaspace.document.DocumentRedisPublisher
import com.ideaspace.document.EditElementCommand
import com.ideaspace.document.FinishedSyncDocCommand
import com.ideaspace.document.InitSyncDocument
import com.ideaspace.document.MoveElementCommand
import com.ideaspace.document.RemoveElementCommand
import com.ideaspace.document.SaveDocCommand
import com.ideaspace.document.SaveLatestRedisEntry
import io.ktor.server.plugins.di.DependencyRegistry
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.slf4j.LoggerFactory
import java.util.concurrent.ConcurrentHashMap
import kotlin.collections.set

class KafkaPartitionProcessor() {
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

        kafkaLogger.info("[SUBMIT TO WORKER] Received record ${record.value()} from topic ${record.topic()}")

        val key = record.key()
        val partitionScope =
            partitionScopePool.computeIfAbsent(key) { CoroutineScope(SupervisorJob() + Dispatchers.IO) }

        val counter = messageCounter.computeIfAbsent(key) {
            0
        }
        messageCounter[key] = counter + 1

        println("KEY ${key} - MESSAGE COUNTER: ${messageCounter[key]}")

        val processId = record.value().processId

        partitionScope.launch {
            val doc = registry.resolve<CrudDocumentRepository>().findById(key)!!

            when (val docEventVale: DocumentSyncEventValue = record.value()) {
                is EditDocEventValue -> {
                    saveOffset(registry, doc, record.offset())
                    val command = registry.resolve<CommandFactory>().createCommand(
                        docEventVale,
                        doc, processId
                    )
                    command.execute()
                }

                is FinishSyncEventValue -> {
                    registry.finish(doc, docEventVale)
                }

                is InitSyncEventValue -> {
                    registry.initDoc(doc, processId)
                }

                is SaveDocEventValue -> {
                    registry.save(doc, docEventVale)
                }
            }


        }

        println("MEMORY WORKER POOL LENGTH: ${partitionScopePool.values.size}")
    }

    private suspend fun saveOffset(registry: DependencyRegistry, doc: BusinessDocument, offset: Long) {
        if (messageCounter[doc.id] == BATCH_LIMIT) {
            println("IO CONTEXT - Process flush kafka message to database")
            registry.resolve<CrudDocumentRepository>().updateOffset(doc.uuid.toString(), offset)
            messageCounter[doc.id] = 0
        }
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


private suspend fun DependencyRegistry.initDoc(doc: BusinessDocument, processId: Long) {
    InitSyncDocument(
        doc, processId,
    ).execute(
        documentPublisher = this.resolve<DocumentRedisPublisher>(),
        elementRepo = this.resolve<ElementRepo>(),
        documentStorage = this.resolve<DocumentStorage>(),
    )
}

private suspend fun DependencyRegistry.finish(doc: BusinessDocument, finishSyncEventValue: FinishSyncEventValue) {
    FinishedSyncDocCommand(doc, finishSyncEventValue).execute(
        documentPublisher = this.resolve<DocumentRedisPublisher>(),
        elementRepo = this.resolve<ElementRepo>(),
        documentStorage = this.resolve<DocumentStorage>(),
    )
}


private suspend fun DependencyRegistry.save(doc: BusinessDocument, saveDocEventValue: SaveDocEventValue) {
    SaveDocCommand(doc, saveDocEventValue).execute(
        documentPublisher = this.resolve<DocumentRedisPublisher>(),
    )
}

