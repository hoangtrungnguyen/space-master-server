package com.ideaspace.config

import com.ideaspace.document.DocumentEventProducer
import com.ideaspace.document.DocumentEventSerializer
import io.ktor.server.application.*
import io.ktor.server.plugins.di.*
import org.apache.kafka.clients.producer.KafkaProducer
import org.apache.kafka.clients.producer.ProducerConfig
import org.apache.kafka.common.serialization.LongSerializer
import java.util.Properties

fun Application.configureDocumentEventProducer() {

    val kafkaConfig = environment.config.config("kafka")
    val servers = kafkaConfig.property("common.bootstrap_servers").getList()
    val clientId = kafkaConfig.property("common.client_id").getString()
    val topicName = kafkaConfig.property("document_event_topic").getString()

    val properties = Properties()
    properties[ProducerConfig.BOOTSTRAP_SERVERS_CONFIG] = servers.joinToString(",")
    properties[ProducerConfig.CLIENT_ID_CONFIG] = clientId

    dependencies {
        provide<DocumentEventProducer> {
            val kafkaProducer = KafkaProducer(
                properties,
                LongSerializer(),
                DocumentEventSerializer()
            )
            return@provide DocumentEventProducer(
                topic = topicName,
                kafkaProducer = kafkaProducer
            )
        }
    }
}