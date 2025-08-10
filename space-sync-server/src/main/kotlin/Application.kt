package com.space

import com.space.infra.datasources.kafka.configureKafka
import io.ktor.server.application.*

fun main(args: Array<String>) {
    io.ktor.server.netty.EngineMain.main(args)
}

fun Application.module() {
//    val kafkaPartitionProcessor = KafkaPartitionProcessor()
//    val kafkaConsumerManager = KafkaConsumerManager(
//        kafkaPartitionProcessor,
//        environment.config.config("kafka")
//    )
//
//    environment.monitor.subscribe(ApplicationStarted) {
//        log.info("Application started. Starting Kafka consumer manager...")
//        kafkaConsumerManager.start()
//    }



    configureHTTP()
    configureMonitoring()
    configureSerialization()
    configureDatabases()
    configureFrameworks()
    configureAdministration()
    configureRouting()
//    configureKafka()

//    environment.monitor.subscribe(ApplicationStopping) {
//        log.info("Application stopping. Shutting down Kafka consumer and processor...")
//        kafkaConsumerManager.shutdown()
//        kafkaPartitionProcessor.shutdown()
//        log.info("Shutdown complete.")
//    }
}
