package com.ideaspace.document

import com.ideaspace.core.kafkaMessage.DocumentSyncEventValue
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.isActive
import kotlinx.coroutines.withContext
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.apache.kafka.clients.consumer.KafkaConsumer
import org.slf4j.LoggerFactory
import java.time.Duration

class DocumentEventConsumer(
    private val topic: String,
    private val kafkaConsumer: KafkaConsumer<Long, DocumentSyncEventValue>,
    private val onRecordReceived: suspend (record: ConsumerRecord<Long, DocumentSyncEventValue>) -> Unit
) {
    private val logger = LoggerFactory.getLogger(DocumentEventConsumer::class.java)

    suspend fun consumeEvents() {
        // Subscribe the consumer to the specified topic.
        kafkaConsumer.subscribe(listOf(topic))
        logger.info("Kafka consumer subscribed to topic: $topic")

        try {
            withContext(Dispatchers.IO) {
                while (coroutineContext.isActive) {
                    val records = kafkaConsumer.poll(Duration.ofMillis(100))
                    for (record in records) {
                        onRecordReceived(record)
                    }
                }
            }
        } catch (e: Exception) {
            logger.error("Error consuming events from topic '$topic'", e)
        } finally {
            // Ensure the consumer is closed properly when the loop exits or an error occurs.
            logger.warn("Closing Kafka consumer for topic '$topic'.")
            close()
        }
    }

    /**
     * Closes the underlying Kafka consumer.
     */
    fun close() {
        kafkaConsumer.close()
    }
}