package com.ideaspace.config

import com.ideaspace.core.kafkaMessage.DocumentEventDeserializer
import com.ideaspace.core.kafkaMessage.DocumentSyncEventValue
import com.ideaspace.core.repository.CrudDocumentRepository
import com.ideaspace.core.repository.ElementRepo
import com.ideaspace.document.DocumentEventConsumer
import com.ideaspace.workers.DocumentStorage
import com.ideaspace.workers.KafkaPartitionProcessor
import com.ideaspace.workers.RedisPublisher
import io.ktor.server.application.Application
import io.ktor.server.application.ApplicationStarted
import io.ktor.server.application.ApplicationStopping
import io.ktor.server.plugins.di.dependencies
import io.ktor.server.plugins.di.resolve
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.apache.kafka.clients.consumer.Consumer
import org.apache.kafka.clients.consumer.ConsumerConfig

import org.apache.kafka.clients.consumer.KafkaConsumer
import org.apache.kafka.clients.producer.ProducerConfig
import org.apache.kafka.common.serialization.LongDeserializer
import java.util.Properties
import kotlin.collections.set

fun Application.configureServerKafka() {

    dependencies.provide {
        KafkaPartitionProcessor(
            resolve<CrudDocumentRepository>(),
            resolve<RedisPublisher>(),
            resolve<ElementRepo>(),
            resolve<DocumentStorage>()
        )
    }

    val kafkaConfig = environment.config.config("kafka")
    val servers = kafkaConfig.property("bootstrap_servers").getList()
    val clientId = kafkaConfig.property("client_id").getString()
    val topicName = kafkaConfig.property("document_event_topic").getString()
    val groupId = kafkaConfig.property("consumer_group_id").getString()

    val properties = Properties()
    properties[ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG] = servers.joinToString(",")
    properties[ConsumerConfig.CLIENT_ID_CONFIG] = clientId
    properties[ConsumerConfig.GROUP_ID_CONFIG] = groupId
    val kafkaConsumer = KafkaConsumer<Long, DocumentSyncEventValue>(
        properties,
        LongDeserializer(),
        DocumentEventDeserializer()
    )

    val documentEventConsumer = DocumentEventConsumer(
        topicName,
        kafkaConsumer
    ) { event ->
        val kafkaPartitionProcessor = dependencies.resolve<KafkaPartitionProcessor>()
        kafkaPartitionProcessor.submit(event)
    }

    monitor.subscribe(ApplicationStarted) {
        // 3. Launch the consumer in a background job
        this.launch {
            documentEventConsumer.consumeEvents()
        }
    }

    monitor.subscribe(ApplicationStopping) {
        runBlocking {
            documentEventConsumer.close()
            dependencies.resolve<KafkaPartitionProcessor>().shutdown()
        }
    }
}
