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

    @Test

    fun `consumeEvents should close consumer even if an exception occurs during processing`() = runTest {
        // Given: A mock record that causes an exception when processed
        val badRecord = mockk<ConsumerRecord<Long, DocumentSyncEventValue>>(relaxed = true)
        val consumerRecords = ConsumerRecords(mapOf(TopicPartition(topic, 0) to listOf(badRecord)))

        // Set up the mock consumer to return the bad record, then throw WakeupException
        every { mockKafkaConsumer.poll(Duration.ofMillis(100)) } returns consumerRecords andThenThrows WakeupException()

        // Set up onRecordReceived to throw an exception
        coEvery { onRecordReceived(badRecord) } throws RuntimeException("Processing error")

        // When: The consumeEvents loop is run
        documentEventConsumer.consumeEvents()

        // Then: Verify that the consumer still attempts to close
        verify { mockKafkaConsumer.subscribe(listOf(topic)) }
        coVerify(exactly = 1) { onRecordReceived(badRecord) }
        verify { documentEventConsumer.close() }
    }

    @Test
    fun `when coroutineContext isActive is false`() = runTest {
        //TODO
//        val records = listOf(
//            mockk<ConsumerRecord<Long, DocumentSyncEventValue>>(relaxed = true)
//        )
//        val consumerRecords = ConsumerRecords(mapOf(TopicPartition(topic, 0) to records))
//
//        // Simulate the scenario where the coroutine context becomes inactive after one poll
//        var firstPoll = true
//        every { mockKafkaConsumer.poll(Duration.ofMillis(100)) } answers {
//            if (firstPoll) {
//                firstPoll = false
//                consumerRecords
//            } else {
//                throw WakeupException()
//            }
//        }
//
//        val job = launch {
//            documentEventConsumer.consumeEvents()
//        }
//
//        // Allow some time for the first poll and processing
//        delay(120)
//
//        // Cancel the job to simulate coroutineContext.isActive becoming false
//        job.cancel()
//
//        // Verify that the consumer was subscribed and processed the records from the first poll
//        verify { mockKafkaConsumer.subscribe(listOf(topic)) }
//        coVerify(exactly = 1) { onRecordReceived(any()) }
//
//        // Verify that close was called due to the loop condition (coroutineContext.isActive) becoming false
//        verify { documentEventConsumer.close() }
    }

    @Test
    fun `consumeEvents should stop when running is set to false`() = runTest {
        // Given: A mock record
        val records = listOf(
            mockk<ConsumerRecord<Long, DocumentSyncEventValue>>(relaxed = true)
        )
        val consumerRecords = ConsumerRecords(mapOf(TopicPartition(topic, 0) to records))

        // Simulate the scenario where running.get() becomes false after one poll
        var firstPoll = true
        every { mockKafkaConsumer.poll(Duration.ofMillis(100)) } answers {
            if (firstPoll) {
                firstPoll = false
                documentEventConsumer.close() // Set running to false
                consumerRecords
            } else {
                throw WakeupException() // Should not be reached if running.get() works
            }
        }

        // When: The consumeEvents loop is run
        documentEventConsumer.consumeEvents()

        // Then: Verify that the consumer was subscribed and processed the records from the first poll
        verify { mockKafkaConsumer.subscribe(listOf(topic)) }
        coVerify(exactly = 1) { onRecordReceived(any()) }

        // Verify that close was called due to the loop condition (running.get()) becoming false
        verify { documentEventConsumer.close() }
    }
}

