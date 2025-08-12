package com.ideaspace.services

import com.ideaspace.services.dto.ServerOperation
import io.github.flaxoos.ktor.server.plugins.kafka.Kafka
import io.github.flaxoos.ktor.server.plugins.kafka.TopicName
import io.github.flaxoos.ktor.server.plugins.kafka.common
import io.github.flaxoos.ktor.server.plugins.kafka.producer
import io.github.flaxoos.ktor.server.plugins.kafka.registerSchemas
import io.ktor.client.HttpClient
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.serialization.kotlinx.json.json
import io.ktor.server.application.Application
import io.ktor.server.application.install

fun Application.configKafka(){

    val schemaRegistryClient = HttpClient {
        install(ContentNegotiation) {
            json()
        }
    }

//    val topicName = environment.config.property("ktor.kafka.topics.0.name").getString()

    val kafkaConfig = environment.config.config("kafka")
    val topicName = "server-operation-topic"
    val operationTopic = TopicName.named(topicName)
    val bootstrapServers = kafkaConfig.property("common.bootstrap.servers").getList()
    val clientId = kafkaConfig.property("clientId").getString()

    install(Kafka) {
        schemaRegistryUrl = environment.config.property("kafka.schema.registry.url").getList().first()
        common { // <-- Define common properties
            this.bootstrapServers = bootstrapServers
            this.retries = 3
            this.clientId = clientId
        }
        producer {

        }

        registerSchemas {
            schemaRegistryClient
            ServerOperation::class to operationTopic
        }
    }
}