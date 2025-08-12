package com.ideaspace.config

import com.ideaspace.core.datasources.kafka.configureKafka
import com.ideaspace.core.repository.CrudDocumentRepository
import com.ideaspace.workers.KafkaPartitionProcessor
import io.ktor.server.application.Application
import io.ktor.server.application.ApplicationStopping
import io.ktor.server.plugins.di.dependencies
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import org.apache.avro.generic.GenericRecord
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.slf4j.LoggerFactory

fun Application.configureServerKafka() {
    val messageHandler: suspend (ConsumerRecord<String, GenericRecord>) -> Unit = { record ->
        val logger = LoggerFactory.getLogger("MessageHandler")
        val operation = record.value()
        val key = record.key()
        val topic = record.topic()
        println("Kafka message handler")
        if(dependencies.resolve<CrudDocumentRepository>().existByUuid(key)) {
            dependencies.resolve<KafkaPartitionProcessor>().submit(record)
        } else {
            logger.error("$key not found in db")
        }
    }

    configureKafka(messageHandler)

    environment.monitor.subscribe(ApplicationStopping) {
        runBlocking {
            dependencies.resolve<KafkaPartitionProcessor>().shutdown()
        }
    }

}