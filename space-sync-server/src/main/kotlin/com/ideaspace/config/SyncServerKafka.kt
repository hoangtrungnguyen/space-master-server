package com.ideaspace.config

import com.ideaspace.core.datasources.kafka.configureKafka
import com.ideaspace.core.kafkaMessage.DocumentSyncEventValue
import com.ideaspace.core.repository.CrudDocumentRepository
import com.ideaspace.workers.KafkaPartitionProcessor
import io.ktor.server.application.Application
import io.ktor.server.application.ApplicationStopping
import io.ktor.server.plugins.di.dependencies
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import org.apache.avro.generic.GenericRecord
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.slf4j.LoggerFactory

fun Application.configureServerKafka() {

    val messageDocumentEventHandler : suspend (ConsumerRecord<Long, GenericRecord>) -> Unit = { record ->
        val logger = LoggerFactory.getLogger("MessageHandler Document event")
        val key = record.key()
        val id = key
        if(dependencies.resolve<CrudDocumentRepository>().existById(id)) {
            dependencies.resolve<KafkaPartitionProcessor>().submit(record)
        } else {
            logger.error("$key not found in db")
        }
    }

    configureKafka( messageDocumentEventHandler)

    monitor.subscribe(ApplicationStopping) {
        runBlocking {
            dependencies.resolve<KafkaPartitionProcessor>().shutdown()
        }
    }

}