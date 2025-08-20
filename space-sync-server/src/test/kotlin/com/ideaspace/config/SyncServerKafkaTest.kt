package com.ideaspace.config

import com.ideaspace.core.kafkaMessage.DocumentSyncEventValue
import com.ideaspace.document.DocumentEventConsumer
import com.ideaspace.workers.KafkaPartitionProcessor
import io.ktor.server.application.ApplicationStarted
import io.ktor.server.application.ApplicationStopping
import io.ktor.server.config.MapApplicationConfig
import io.ktor.server.plugins.di.dependencies
import io.ktor.server.testing.testApplication
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runTest
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.apache.kafka.clients.consumer.KafkaConsumer
import org.junit.jupiter.api.Test
import kotlin.test.assertNotNull

@ExperimentalCoroutinesApi
class SyncServerKafkaTest {

    @Test
    fun `test configureServerKafka`() = runTest {
        val mockKafkaConsumer = mockk<KafkaConsumer<Long, DocumentSyncEventValue>>(relaxed = true)
        val mockPartitionProcessor = mockk<KafkaPartitionProcessor>(relaxed = true)

        testApplication {
            environment {
                config = MapApplicationConfig(
                    "kafka.client_id" to "test-client",
                    "kafka.document_event_topic" to "test-topic",
                    "kafka.consumer_group_id" to "test-group",
                ).apply {
                    put(
                        path = "kafka.bootstrap_servers",
                        values = listOf("localhost:9092")
                    )
                }
            }

            application {
                dependencies.provide { mockPartitionProcessor }
                configureServerKafka()
                assertNotNull(dependencies.resolve<KafkaPartitionProcessor>())
            }
        }
    }

    @Test
    fun `test kafka consumer startup and shutdown`() = runTest {
        val mockDocumentEventConsumer = mockk<DocumentEventConsumer>(relaxed = true)
        val mockPartitionProcessor = mockk<KafkaPartitionProcessor>(relaxed = true)

        coEvery { mockDocumentEventConsumer.consumeEvents() } returns Unit
        coEvery { mockDocumentEventConsumer.close() } returns Unit
        coEvery { mockPartitionProcessor.shutdown() } returns Unit

        testApplication {
            application {
                dependencies.provide { mockPartitionProcessor }
                val app = this
                app.monitor.subscribe(ApplicationStarted) {
                    app.launch {
                        mockDocumentEventConsumer.consumeEvents()
                    }
                }
                app.monitor.subscribe(ApplicationStopping) {
                    mockDocumentEventConsumer.close()
                    mockPartitionProcessor.shutdown()
                }
            }
        }
        coVerify { mockDocumentEventConsumer.consumeEvents() }
        coVerify { mockDocumentEventConsumer.close() }
        coVerify { mockPartitionProcessor.shutdown() }
    }

    @Test
    fun `test event processing`() = runTest {
        val mockPartitionProcessor = mockk<KafkaPartitionProcessor>(relaxed = true)
        val mockRecord = mockk<ConsumerRecord<Long, DocumentSyncEventValue>>(relaxed = true)


        testApplication {
            val onRecordReceived: suspend (record: ConsumerRecord<Long, DocumentSyncEventValue>) -> Unit = { record ->
                mockPartitionProcessor.submit(application.dependencies, record)
            }
            application {
                dependencies.provide { mockPartitionProcessor }
                onRecordReceived(mockRecord)
            }
        }
        coVerify { mockPartitionProcessor.submit(any(), mockRecord) }
    }
}