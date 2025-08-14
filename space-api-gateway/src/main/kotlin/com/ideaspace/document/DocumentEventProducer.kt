package com.ideaspace.document

import com.ideaspace.core.kafkaMessage.DocumentSyncEventValue
import org.apache.kafka.clients.producer.KafkaProducer
import org.apache.kafka.clients.producer.ProducerRecord
import org.slf4j.LoggerFactory
import kotlinx.coroutines.future.await
import java.util.concurrent.CompletableFuture

class DocumentEventProducer(
    private val topic: String,
    private val kafkaProducer: KafkaProducer<Long, DocumentSyncEventValue>
) {
    
    private val logger = LoggerFactory.getLogger(DocumentEventProducer::class.java)
    
    suspend fun sendEvent(key: Long, event: DocumentSyncEventValue) {
        try {
            logger.info("Sending document event: $event to topic: $topic with key: $key")
            
            val record = ProducerRecord(topic, key, event)
            val future = CompletableFuture<Unit>()
            
            kafkaProducer.send(record) { metadata, exception ->
                if (exception != null) {
                    logger.error("Failed to send document event", exception)
                    future.completeExceptionally(exception)
                } else {
                    logger.info("Successfully sent document event to partition: ${metadata.partition()}, offset: ${metadata.offset()}")
                    future.complete(Unit)
                }
            }
            
            future.await()
        } catch (e: Exception) {
            logger.error("Error sending document event", e)
            throw e
        }
    }
    
    fun close() {
        kafkaProducer.close()
    }
}