package com.space.com.ideaspace.config

import com.space.com.ideaspace.services.KafkaProducerService
import com.space.com.ideaspace.session.SessionManager
import com.space.com.ideaspace.space.IdeaSpaceSocketHandler
import com.space.com.ideaspace.space.client.IdeaSpaceServerClient
import com.space.com.ideaspace.space.handlers.DocumentOperationHandler
import com.space.com.ideaspace.space.models.OperationType
import com.space.com.ideaspace.space.repository.WhiteboardRepository
import com.space.com.ideaspace.space.service.IdeaSpaceSessionService
import io.github.flaxoos.ktor.server.plugins.kafka.kafkaProducer
import io.ktor.client.HttpClient
import io.ktor.server.application.Application
import io.ktor.server.plugins.di.dependencies
import io.ktor.server.plugins.di.resolve
import kotlin.to


fun Application.configureFrameworks() {

    dependencies {
        provide<IdeaSpaceServerClient> {
            IdeaSpaceServerClient(
                resolve<HttpClient>()
            )
        }
        provide<WhiteboardRepository> {
            WhiteboardRepository(
                resolve<IdeaSpaceServerClient>()
            )
        }

        provide<IdeaSpaceSessionService> {
            IdeaSpaceSessionService(
                syncServerClient = resolve<IdeaSpaceServerClient>(),
                repository = resolve<WhiteboardRepository>()
            )
        }

        provide<IdeaSpaceSocketHandler> {
            IdeaSpaceSocketHandler(
                sessionService = resolve<IdeaSpaceSessionService>(),
                sessionManager = SessionManager(),
                handlers = mapOf(
                    // OperationType.Document to DocumentOperationHandler()
                )
            )
        }

        provide<KafkaProducerService>{
            KafkaProducerService(
                 this@configureFrameworks.kafkaProducer!!
            )
        }
    }

}
