package com.ideaspace.core.kafkaMessage

import kotlinx.coroutines.future.await
import org.apache.kafka.clients.producer.KafkaProducer
import org.apache.kafka.clients.producer.ProducerRecord
import org.slf4j.LoggerFactory
import java.util.concurrent.CompletableFuture

/**
 * By default, a Kafka producer batches messages before sending —
 * it doesn't necessarily send each record immediately.
 *
 * - Kafka producer collects messages per topic-partition in a memory buffer (`buffer.memory`).
 * - When a batch reaches a certain size (`batch.size`) or a certain time passes (`linger.ms`), it gets sent to the broker.
 * - This batching happens per partition, so multiple small sends to the same partition can be merged into one network request.
 */
class DocumentEventProducer(
    private val topic: String,
    private val kafkaProducer: KafkaProducer<Long, DocumentSyncEventValue>
) {

    private val logger = LoggerFactory.getLogger(DocumentEventProducer::class.java)

    suspend fun sendEvent(key: Long, event: DocumentSyncEventValue) {
        val record = ProducerRecord(topic, key, event)
        val future = CompletableFuture<Unit>()

        kafkaProducer.send(record) { metadata, exception ->
            if (exception != null) {
                logger.error("Failed to send document event", exception)
                future.completeExceptionally(exception)
            } else {
                future.complete(Unit)
            }
        }

        future.await()
    }

    fun close() {
        kafkaProducer.flush()
        kafkaProducer.close()
    }
}