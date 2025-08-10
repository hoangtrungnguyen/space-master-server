package com.space.infra.datasources.kafka

import io.ktor.server.application.Application
import org.apache.kafka.clients.admin.AdminClient
import org.apache.kafka.clients.admin.AdminClientConfig
import org.apache.kafka.clients.admin.NewPartitions
import org.apache.kafka.clients.admin.NewTopic
import org.apache.kafka.common.errors.TopicExistsException
import java.util.Properties
import java.util.concurrent.ExecutionException
import kotlin.use



data class CreateSpacePartitionDto(
    val key: String,
)

fun produceMessageKey(){
    val numberOfPartition: Int = 100

}


fun Application.createOrAlterTopicPartitions(topicName: String, desiredPartitions: Int) {
    val kafkaConfig = environment.config.config("kafka")
    val bootstrapServers = kafkaConfig.property("bootstrapServers").getList()

    val props = Properties().apply {
        put(AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers)
    }

    // use-with-resources ensures the AdminClient is closed automatically
    AdminClient.create(props).use { adminClient ->
        try {
            val existingTopics = adminClient.listTopics().names().get()

            if (topicName in existingTopics) {
                // Topic exists, check its partition count
                val description = adminClient.describeTopics(listOf(topicName)).all().get()[topicName]!!
                val currentPartitions = description.partitions().size

                if (currentPartitions < desiredPartitions) {
                    println("Topic '$topicName' exists with $currentPartitions partitions. Increasing to $desiredPartitions...")
                    val newPartitions = NewPartitions.increaseTo(desiredPartitions)
                    adminClient.createPartitions(mapOf(topicName to newPartitions)).all().get()
                    println("Successfully increased partitions for topic '$topicName' to $desiredPartitions.")
                } else {
                    println("Topic '$topicName' already exists with sufficient partitions ($currentPartitions). No action needed.")
                }
            } else {
                // Topic does not exist, create it with the desired number of partitions
                println("Topic '$topicName' does not exist. Creating it with $desiredPartitions partitions...")
                // Using replication factor of 1, which is standard for a single-broker local setup.
                val newTopic = NewTopic(topicName, desiredPartitions, 1.toShort())
                adminClient.createTopics(listOf(newTopic)).all().get()
                println("Successfully created topic '$topicName' with $desiredPartitions partitions.")
            }
        } catch (e: ExecutionException) {
            // Kafka wraps exceptions in ExecutionException. We check the cause.
            if (e.cause is TopicExistsException) {
                // This can happen in a race condition if another instance creates the topic
                // between our check and our creation attempt. It's safe to ignore.
                println("Topic already created by another instance, which is fine. Continuing.")
            } else {
                println("Error during topic administration: ${e.cause?.message}")
                throw e // Re-throw for other critical errors
            }
        }
    }
}