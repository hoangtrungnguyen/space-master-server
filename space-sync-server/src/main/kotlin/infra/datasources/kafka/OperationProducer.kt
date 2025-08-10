package com.space.infra.datasources.kafka

import com.space.features.operation.ServerOperation
import io.ktor.server.config.ApplicationConfig
import org.apache.kafka.clients.producer.KafkaProducer
import org.apache.kafka.clients.producer.ProducerConfig
import org.apache.kafka.clients.producer.ProducerRecord
import java.util.Properties

/**
 * A service responsible for producing ServerOperation messages to a Kafka topic.
 * It is fully configured by the application's configuration file.
 *
 * @param config The application configuration, injected via Koin.
 */
class OperationProducer(config: ApplicationConfig) {

    private val kafkaConfig = config.config("kafka")
    private val topic = kafkaConfig.property("topic").getString()

    private val producer: KafkaProducer<String, ServerOperation> by lazy {
        val props = Properties()
        val producerConfig = kafkaConfig.config("producer")

        // --- Core Connection Properties ---
        props[ProducerConfig.BOOTSTRAP_SERVERS_CONFIG] = kafkaConfig.property("bootstrapServers").getString()
        // The Schema Registry URL is a custom property for the Confluent serializers
        props["schema.registry.url"] = kafkaConfig.property("schemaRegistryUrl").getString()

        // --- Serializer Configuration (Read from YAML) ---
        // This is now completely dynamic.
        props[ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG] = producerConfig.property("keySerializer").getString()
        props[ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG] = producerConfig.property("valueSerializer").getString()

        // --- Reliability and Performance Settings (Read from YAML) ---
        props[ProducerConfig.ACKS_CONFIG] = producerConfig.property("acks").getString()
        props[ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG] = producerConfig.property("enableIdempotence").getString()

        KafkaProducer(props)
    }

    /**
     * Sends a ServerOperation to the configured Kafka topic.
     */
    fun sendOperation(boardId: String, serverOperation: ServerOperation) {
        val record = ProducerRecord(topic, boardId, serverOperation)
        try {
            producer.send(record)
        } catch (e: Exception) {
            // In a real app, you'd inject and use a proper SLF4J logger
            System.err.println("Failed to send operation to Kafka: ${e.message}")
            e.printStackTrace()
        }
    }

    fun close() {
        producer.close()
    }
}