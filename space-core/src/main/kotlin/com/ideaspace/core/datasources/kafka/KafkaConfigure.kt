package com.ideaspace.core.datasources.kafka

import com.ideaspace.core.datasources.kafka.dto.ServerOperation
import io.github.flaxoos.ktor.server.plugins.kafka.AbstractKafkaConfig
import io.github.flaxoos.ktor.server.plugins.kafka.Kafka
import io.github.flaxoos.ktor.server.plugins.kafka.KafkaConsumerConfig
import io.ktor.server.application.Application
import io.ktor.server.application.install
import io.github.flaxoos.ktor.server.plugins.kafka.MessageTimestampType
import io.github.flaxoos.ktor.server.plugins.kafka.TopicName
import io.github.flaxoos.ktor.server.plugins.kafka.common
import io.github.flaxoos.ktor.server.plugins.kafka.consumer
import io.github.flaxoos.ktor.server.plugins.kafka.consumerConfig
import io.github.flaxoos.ktor.server.plugins.kafka.consumerRecordHandler
import io.github.flaxoos.ktor.server.plugins.kafka.registerSchemas
import io.github.flaxoos.ktor.server.plugins.kafka.topic
import io.ktor.client.HttpClient
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.serialization.kotlinx.json.json
import io.ktor.server.plugins.di.dependencies
import org.slf4j.LoggerFactory

fun Application.configureKafka(
    consumerConfig: KafkaConsumerConfig.() -> Unit = { }
) {

    //logger
    val kafkaLogger = LoggerFactory.getLogger("kafka-consumer")

    // Load configuration from application.yaml to avoid hardcoding
    val kafkaConfig = environment.config.config("kafka")
    val schemaRegistryUrl = kafkaConfig.property("schemaRegistryUrl").getString() // Load the missing URL
    val bootstrapServers = kafkaConfig.property("bootstrapServers").getList()
    val clientId = kafkaConfig.property("clientId").getString()
    val consumerGroupId = kafkaConfig.property("consumerGroupId").getString()
    val topicName = kafkaConfig.property("topic").getString()
    val partitions = kafkaConfig.property("topicDefaults.partitions").getString().toInt()
    val replicas = kafkaConfig.property("topicDefaults.replicas").getString().toInt()

    val operationTopic = TopicName.named(topicName)

    // The Kafka plugin needs an HttpClient to talk to the Schema Registry.
    // This client must be configured with the correct content negotiation plugin.
    val schemaRegistryClient = HttpClient {
        install(ContentNegotiation) {
            json()
        }
    }
    install(Kafka) {

        this.schemaRegistryUrl = schemaRegistryUrl
        common { // <-- Define common properties
            this.bootstrapServers = bootstrapServers
            this.retries = 3
            this.clientId = clientId
        }

        topic(operationTopic) {
            this.partitions = partitions
            this.replicas = replicas.toShort()
            configs {
                messageTimestampType = MessageTimestampType.CreateTime
            }
        }

//        producer { }
        consumer { // <-- Creates a consumer
            groupId = consumerGroupId
        }
        consumerConfig {
            consumerRecordHandler(operationTopic){

            }
        }
        registerSchemas {
            using {
                schemaRegistryClient
            }
            ServerOperation::class at operationTopic
        }
    }


}