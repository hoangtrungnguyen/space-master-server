package com.ideaspace.document

import com.ideaspace.core.kafkaMessage.DocumentSyncEventValue
import io.mockk.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runTest
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.apache.kafka.clients.consumer.ConsumerRecords
import org.apache.kafka.clients.consumer.KafkaConsumer
import org.apache.kafka.common.TopicPartition
import org.apache.kafka.common.errors.WakeupException
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.time.Duration

@ExperimentalCoroutinesApi
class DocumentEventConsumerTest {

    private lateinit var mockKafkaConsumer: KafkaConsumer<Long, DocumentSyncEventValue>
    private lateinit var onRecordReceived: suspend (record: ConsumerRecord<Long, DocumentSyncEventValue>) -> Unit
    private lateinit var documentEventConsumer: DocumentEventConsumer

    private val topic = "test-topic"

    @BeforeEach
    fun setUp() {
        // Mock the KafkaConsumer and the callback function
        mockKafkaConsumer = mockk(relaxed = true)
        onRecordReceived = spyk({ _ -> }) // Use spyk to verify calls to the lambda
        documentEventConsumer = DocumentEventConsumer(topic, mockKafkaConsumer, onRecordReceived)
    }

    @Test
    fun `consumeEvents should subscribe, poll for records, and process them`() = runTest {
        // Given: A list of mock records to be returned by the consumer
        val records = listOf(
            mockk<ConsumerRecord<Long, DocumentSyncEventValue>>(relaxed = true),
            mockk<ConsumerRecord<Long, DocumentSyncEventValue>>(relaxed = true)
        )
        val consumerRecords = ConsumerRecords(mapOf(TopicPartition(topic, 0) to records))

        // Set up the mock consumer to return records on the first poll, then trigger a shutdown
        every<ConsumerRecords<Long, DocumentSyncEventValue>> {
            mockKafkaConsumer.poll(Duration.ofMillis(100))
        } returns consumerRecords andThenThrows WakeupException()

        // When: The consumeEvents loop is run
        documentEventConsumer.consumeEvents()

        // Then: Verify that the consumer subscribed, polled, and processed each record
        verify { mockKafkaConsumer.subscribe(listOf(topic)) }
        coVerify(exactly = 2) { onRecordReceived(any()) }
        verify { documentEventConsumer.close() }
    }
}
