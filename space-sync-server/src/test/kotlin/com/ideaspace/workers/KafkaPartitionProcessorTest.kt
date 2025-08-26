package com.ideaspace.workers

import com.ideaspace.core.kafkaMessage.*
import com.ideaspace.core.models.BusinessDocument
import com.ideaspace.core.repository.CrudDocumentRepository
import com.ideaspace.core.repository.ElementRepo
import com.ideaspace.document.BaseDocCommand
import com.ideaspace.document.CommandFactory
import com.ideaspace.document.DocumentRedisPublisher
import io.ktor.server.plugins.di.DependencyRegistry
import io.mockk.*
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.JsonNull
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import java.util.*

class KafkaPartitionProcessorTest {

    private lateinit var kafkaPartitionProcessor: KafkaPartitionProcessor
    private lateinit var dependencyRegistry: DependencyRegistry
    private lateinit var crudDocumentRepository: CrudDocumentRepository
    private lateinit var documentRedisPublisher: DocumentRedisPublisher
    private lateinit var elementRepo: ElementRepo
    private lateinit var documentStorage: DocumentStorage
    private lateinit var logPublisher: LogPublisher
    private lateinit var commandFactory: CommandFactory
    private lateinit var command: BaseDocCommand

    @BeforeEach
    fun setUp() {
        kafkaPartitionProcessor = KafkaPartitionProcessor()
        dependencyRegistry = mockk(relaxed = true)
        crudDocumentRepository = mockk(relaxed = true)
        documentRedisPublisher = mockk(relaxed = true)
        elementRepo = mockk(relaxed = true)
        documentStorage = mockk(relaxed = true)
        logPublisher = mockk(relaxed = true)
        commandFactory = mockk(relaxed = true)
        command = mockk(relaxed = true)
    }

    private suspend fun setupDependencies() {
        coEvery { dependencyRegistry.resolve<CrudDocumentRepository>() } returns crudDocumentRepository
        coEvery { dependencyRegistry.resolve<DocumentRedisPublisher>() } returns documentRedisPublisher
        coEvery { dependencyRegistry.resolve<ElementRepo>() } returns elementRepo
        coEvery { dependencyRegistry.resolve<DocumentStorage>() } returns documentStorage
        coEvery { dependencyRegistry.resolve<LogPublisher>() } returns logPublisher
        coEvery { dependencyRegistry.resolve<CommandFactory>() } returns commandFactory

        val mockDocument = mockk<BusinessDocument>(relaxed = true) {
            every { id } returns 1L
            every { uuid } returns UUID.randomUUID()
        }
        coEvery { crudDocumentRepository.findById(any()) } returns mockDocument
        every { commandFactory.createCommand(any(), any(), any()) } returns command
        coEvery { command.execute() } just runs
    }

    private fun createConsumerRecord(key: Long, value: DocumentSyncEventValue, offset: Long = 0L): ConsumerRecord<Long, DocumentSyncEventValue> {
        return ConsumerRecord("test-topic", 0, offset, key, value)
    }

    @Nested
    @DisplayName("Event Processing Logic")
    inner class EventProcessing {

        @Test
        @DisplayName("should process AddElementPayload for EditDocEventValue")
        fun processAddElementPayload() = runTest {
            setupDependencies()
            val addElementPayload =
                AddElementPayload(element = AddElement(uuid = UUID.randomUUID(), type = "test", value = JsonNull))
            val event = EditDocEventValue(
                docId = 1L,
                processId = 2L,
                userId = 3L,
                windowId = 5L,
                payload = addElementPayload
            )
            val record = createConsumerRecord(1L, event)

            every { documentStorage.documentsMap[1L] } returns mockk(relaxed = true) {
                every { exist(any()) } returns false
            }
            coEvery { documentRedisPublisher.publishEditDocEvent(any(), any(), any()) } returns "redis-entry"

            kafkaPartitionProcessor.submit(dependencyRegistry, record)
            coVerify { crudDocumentRepository.findById(any()) }
            coVerify(exactly = 1) { command.execute() }
        }
    }
}
