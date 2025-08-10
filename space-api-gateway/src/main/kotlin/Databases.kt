package com.space

import com.space.services.configKafka
import com.space.services.dto.ServerOperation
import io.github.flaxoos.ktor.server.plugins.kafka.Kafka
import io.github.flaxoos.ktor.server.plugins.kafka.MessageTimestampType
import io.github.flaxoos.ktor.server.plugins.kafka.TopicName
import io.github.flaxoos.ktor.server.plugins.kafka.common
import io.github.flaxoos.ktor.server.plugins.kafka.consumerConfig
import io.github.flaxoos.ktor.server.plugins.kafka.producer
import io.github.flaxoos.ktor.server.plugins.kafka.registerSchemas
import io.github.flaxoos.ktor.server.plugins.kafka.topic
import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.logging.*
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.serialization.kotlinx.json.json
import io.ktor.server.application.*
import io.ktor.server.plugins.di.*
import kotlinx.serialization.json.Json


fun Application.configureDatabases() {

//    configKafka()
    dependencies {
        provide<HttpClient> {
            HttpClient(CIO) {

                install(ContentNegotiation) {
                    json(Json {
                        prettyPrint = true
                        isLenient = true
                        ignoreUnknownKeys = true // Very useful for resilient clients
                    })
                }

                install(Logging) {
                    logger = Logger.DEFAULT
                    level = LogLevel.HEADERS
                }

                defaultRequest {
                    contentType(ContentType.Application.Json)
                    url("http://127.0.0.1:9099")
                }
                engine {
                    requestTimeout = 30_000
                }
            }
        }

    }

}

