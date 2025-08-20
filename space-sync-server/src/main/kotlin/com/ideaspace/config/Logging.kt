package com.ideaspace.config

import com.ideaspace.core.utils.LogData
import com.ideaspace.core.utils.LogDataEventSerializer
import com.ideaspace.workers.KafkaLogPublisher
import com.ideaspace.workers.LogPublisher
import io.confluent.kafka.serializers.KafkaJsonSerializer
import io.ktor.server.application.Application
import io.ktor.server.plugins.di.dependencies
import kotlinx.serialization.json.JsonElement
import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.clients.producer.KafkaProducer
import org.apache.kafka.common.serialization.LongSerializer
import org.apache.kafka.common.serialization.StringSerializer
import java.util.Properties
import kotlin.collections.set

fun Application.configureLogging() {

    val kafkaConfig = environment.config.config("kafka")
    val servers = kafkaConfig.property("bootstrap_servers").getList()
    val clientId = kafkaConfig.property("client_id").getString()

    val properties = Properties()
    properties[ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG] = servers.joinToString(",")
    properties[ConsumerConfig.CLIENT_ID_CONFIG] = clientId

    dependencies.provide<LogPublisher> {

        val kafkaProducer = KafkaProducer<Long, LogData>(
            properties,
            LongSerializer(),
            LogDataEventSerializer()
        )

        return@provide KafkaLogPublisher(
            kafkaProducer = kafkaProducer
        )
    }
}