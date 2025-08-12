package com.ideaspace.config


import com.ideaspace.document.provideDocumentDependencies
import com.ideaspace.workers.KafkaPartitionProcessor
import io.ktor.server.application.*
import io.ktor.server.plugins.di.DependencyRegistry
import io.ktor.server.plugins.di.dependencies


fun Application.configureFrameworks() {
    val kafkaPartitionProcessor = KafkaPartitionProcessor()
    dependencies {
        provide<KafkaPartitionProcessor> { kafkaPartitionProcessor }
        provideDocumentDependencies()
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
