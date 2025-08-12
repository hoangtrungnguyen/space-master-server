package com.ideaspace.config

import com.ideaspace.core.datasources.kafka.configureKafka
import io.ktor.server.application.Application
import org.apache.avro.generic.GenericRecord
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.slf4j.LoggerFactory

fun Application.configureServerKafka(){
    val messageHandler: suspend (ConsumerRecord<String, GenericRecord>) -> Unit = { record ->
        val logger = LoggerFactory.getLogger("MessageHandler")
        val operation = record.value()
        val key = record.key()
        val topic = record.topic()
        println("Kafka message handler")
        println(record)
    }

    configureKafka(messageHandler)
}