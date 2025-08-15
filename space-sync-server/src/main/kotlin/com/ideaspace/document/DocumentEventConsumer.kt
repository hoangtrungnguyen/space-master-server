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
            // Use withContext(Dispatchers.IO) to move the blocking poll operation
            // off the main thread pool, which is crucial for a responsive server.
            withContext(Dispatchers.IO) {
                // The loop continues as long as the parent coroutine is active.
                // This allows for graceful shutdown.
                while (coroutineContext.isActive) {
                    // Poll Kafka for new records with a 1-second timeout.
                    val records = kafkaConsumer.poll(Duration.ofMillis(1000))

                    // Process each record received from the poll.
                    for (record in records) {
                        onRecordReceived(record)
                    }

                    // If you have 'enable.auto.commit' set to 'false' in your consumer properties,
                    // you would commit the offsets manually here.
                    // For example: kafkaConsumer.commitAsync()
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