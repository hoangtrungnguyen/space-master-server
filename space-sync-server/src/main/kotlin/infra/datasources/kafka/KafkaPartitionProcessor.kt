package com.space.infra.datasources.kafka

import io.github.flaxoos.ktor.server.plugins.kafka.KafkaRecordKey
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import org.apache.avro.generic.GenericRecord
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.apache.kafka.common.TopicPartition
import org.slf4j.LoggerFactory
import java.util.concurrent.ConcurrentHashMap

class KafkaPartitionProcessor(
//    private val inMemoryService: InMemoryService,
//    private val dbService: DbPersistenceService,
//    private val transformService: TransformService,
) {
    companion object {
        var count = 0
    }

    init {
        count++
        println("KafkaPartitionProcessor ${count}")
    }

    //partition -> coroutine
    private val partitionScopePool = ConcurrentHashMap<Int, CoroutineScope>()
    private val kafkaLogger = LoggerFactory.getLogger("kafka-consumer")
//    private val dbWorkers = ConcurrentHashMap<TopicPartition, DbPersistenceWorker>()
//    private val inMemoryWorkers = ConcurrentHashMap<TopicPartition, InMemoryWorker>()

    fun submit(record: ConsumerRecord<KafkaRecordKey, GenericRecord>) {
        kafkaLogger.info("[SUBMIT TO WORKER] Received record ${record.value()} from topic ${record.topic()}")

        val partition = record.partition()
        val partitionScope =
            partitionScopePool.computeIfAbsent(partition) { CoroutineScope(SupervisorJob() + Dispatchers.IO) }

//        val dbWorker = dbWorkers.computeIfAbsent(partition) {
//            DbPersistenceWorker(partitionScope, it, dbService, transformService)
//        }
//

        partitionScope.launch {


        }
    }

    fun shutdown() {
//        println("Shutting down all partition processors by cancelling the scope...")
//         Gracefully shut down each individual DB worker.
//        dbWorkers.values.forEach { it.shutdown() }
//        dbWorkers.clear()
//
//        inMemoryWorkers.values.forEach { it.shutdown() }
//        inMemoryWorkers.clear()
//
//        partitionScopePool.values.forEach {
//            it.cancel()
//        }
//        partitionScopePool.clear()
//
//        println("All partition processors shut down.")
    }
}