package com.space

import com.space.features.space.IdeaSpaceSocketHandler
import com.space.features.space.client.IdeaSpaceServerClient
import com.space.features.space.handlers.DocumentOperationHandler
import com.space.features.space.models.OperationType
import com.space.features.space.repository.WhiteboardRepository
import com.space.features.space.service.IdeaSpaceSessionService
import com.space.services.KafkaProducerService
import com.space.session.SessionManager
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
        provide<WhiteboardRepository> { WhiteboardRepository(
            resolve<IdeaSpaceServerClient>()
        ) }

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
                    OperationType.Document to DocumentOperationHandler()
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
