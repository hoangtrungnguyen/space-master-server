package com.ideaspace

import com.ideaspace.core.datasources.kafka.configureKafka
import com.ideaspace.config.configureDatabases
import com.ideaspace.config.configureFrameworks
import com.ideaspace.config.configureHTTP
import com.ideaspace.config.configureMonitoring
import com.ideaspace.config.configureRouting
import com.ideaspace.config.configureSerialization
import com.ideaspace.workers.KafkaPartitionProcessor
import io.ktor.server.application.*
import io.ktor.server.netty.EngineMain
import io.ktor.server.plugins.di.dependencies

fun main(args: Array<String>) {
    EngineMain.main(args)
}

fun Application.module() {

    configureHTTP()
    configureMonitoring()
    configureSerialization()
    configureDatabases()
    configureFrameworks()
    configureRouting()
    configureKafka()

}