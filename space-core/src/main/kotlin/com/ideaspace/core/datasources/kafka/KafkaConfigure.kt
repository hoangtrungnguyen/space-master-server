package com.ideaspace.core.datasources.kafka

import io.confluent.kafka.serializers.KafkaAvroDeserializer
import io.confluent.kafka.serializers.KafkaJsonDeserializer
import io.github.flaxoos.ktor.server.plugins.kafka.*
import io.ktor.server.application.*
import org.apache.avro.generic.GenericRecord
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.apache.kafka.common.serialization.StringDeserializer
import org.slf4j.LoggerFactory
import java.util.LinkedHashMap

fun Application.configureKafka(
    onServerOperationMessage: suspend (record: ConsumerRecord<String, GenericRecord
            >) -> Unit,
    onDocumentSyncEvent: suspend (record: ConsumerRecord<String, GenericRecord>) -> Unit
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
    val documentEventTopic = TopicName.named(kafkaConfig.property("topicDocumentEvent").getString())


    install(Kafka) {
        this.admin {

        }
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

        topic(documentEventTopic) {
            this.partitions = partitions
            this.replicas = replicas.toShort()
            configs {
                messageTimestampType = MessageTimestampType.CreateTime
            }
        }

        consumer { // <-- Creates a consumer
            groupId = consumerGroupId
            keyDeserializerClass = StringDeserializer::class.java.name
            valueDeserializerClass = KafkaJsonDeserializer::class.java.name
        }

        consumerConfig {
            consumerRecordHandler(operationTopic){ record ->
                println("consumerRecordHandler - ${record}")
                onServerOperationMessage(record)
            }
            consumerRecordHandler(documentEventTopic){ record ->
                println("consumerRecordHandler - ${record}")
                println((record.value() as LinkedHashMap<*, *>)["sync_op"])
                onDocumentSyncEvent(record)
            }

        }

    }


}