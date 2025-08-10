package com.space.services

import io.github.flaxoos.ktor.server.plugins.kafka.TopicName
import io.ktor.server.application.Application
import org.slf4j.LoggerFactory


fun Application.connectKafka(){

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

}

class KafkaConnector(){

}
//class KafkaProducerService {
//
//     fun publish(topic: String, message: Any){
//
//    }
//}


