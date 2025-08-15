package com.ideaspace.config

import com.ideaspace.core.repository.CrudDocumentRepository
import com.ideaspace.core.repositoryImpl.CrudDocumentRepositoryImpl
import com.ideaspace.workers.KafkaPartitionProcessor
import com.ideaspace.workers.RedisPublisher
import io.ktor.server.application.*
import io.ktor.server.plugins.di.DependencyRegistry
import io.ktor.server.plugins.di.dependencies


fun Application.configureFrameworks() {
    dependencies {
        provideKafka()
    }
}

fun DependencyRegistry.provideKafka() {
     provide{ KafkaPartitionProcessor(
         resolve<CrudDocumentRepository>(),
         resolve<RedisPublisher>()
     ) }
}
