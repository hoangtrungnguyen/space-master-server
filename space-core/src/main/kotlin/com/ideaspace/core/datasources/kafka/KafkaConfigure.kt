package com.ideaspace.core.datasources.kafka

import io.confluent.kafka.serializers.KafkaAvroDeserializer
import io.confluent.kafka.serializers.KafkaJsonDeserializer
import io.github.flaxoos.ktor.server.plugins.kafka.*
import io.github.flaxoos.ktor.server.plugins.kafka.Defaults.DEFAULT_CONSUMER_POLL_FREQUENCY_MS
import io.ktor.server.application.*
import org.apache.avro.generic.GenericRecord
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.apache.kafka.common.serialization.LongDeserializer
import org.apache.kafka.common.serialization.StringDeserializer
import org.slf4j.LoggerFactory
import java.util.LinkedHashMap
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds

fun Application.configureKafka(
    onDocumentSyncEvent: suspend (record: ConsumerRecord<Long, GenericRecord>) -> Unit
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
            consumerRecordHandler(documentEventTopic){ record ->
                val eventRecord = ConsumerRecord<Long, GenericRecord>(
                    record.topic(),
                    record.partition(),
                    record.offset(),
                    record.timestamp(),
                    record.timestampType(),
                    record.serializedKeySize(),
                    record.serializedValueSize(),
                    record.key().toByteArray().toLong(),
                    record.value(),
                    record.headers(),
                    record.leaderEpoch()
                )
                onDocumentSyncEvent(eventRecord)
            }
        }
    }
}

fun ByteArray.toLong(): Long {
    require(this.size == 8) { "Byte array must contain exactly 8 bytes for conversion to Long." }

    var result: Long = 0
    for (i in 0 until 8) {
        result = (result shl 8) or (this[i].toLong() and 0xFF)
    }
    return result
}