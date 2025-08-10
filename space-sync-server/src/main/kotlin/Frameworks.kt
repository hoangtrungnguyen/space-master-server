package com.space


import com.space.features.space.provideSpaceDependencies
import com.space.infra.datasources.kafka.KafkaPartitionProcessor
import io.ktor.server.application.*
import io.ktor.server.plugins.di.DI
import io.ktor.server.plugins.di.DependencyRegistry
import io.ktor.server.plugins.di.dependencies
import io.ktor.server.plugins.di.provide


fun Application.configureFrameworks() {
    val kafkaPartitionProcessor = KafkaPartitionProcessor()

    dependencies {
        provide<KafkaPartitionProcessor> { kafkaPartitionProcessor }
        provideSpaceDependencies()
    }


}

fun DependencyRegistry.provideKafka() {
//    // The factory is provided first. Ktor injects its dependencies (e.g., OperationRouter).
//     provide{ PartitionProcessorFactory(get()) }
//
//
//    // The manager is provided next. Ktor injects the Application and the factory.
//    // It's a singleton, so only one instance will be created and started.
//    provide {
//        KafkaManager(application, get())
//    }
}
