package com.ideaspace.config

import io.ktor.server.application.*
import io.ktor.server.plugins.di.*
import io.lettuce.core.RedisClient
import kotlinx.coroutines.runBlocking

fun Application.configureRedis() {

    val redisClient = RedisClient.create("redis://localhost:6379")
    dependencies {
        provide<RedisClient> {
            redisClient
        }
    }
    
    monitor.subscribe(ApplicationStopping) {
        runBlocking {
            redisClient.shutdown()
        }
    }
}