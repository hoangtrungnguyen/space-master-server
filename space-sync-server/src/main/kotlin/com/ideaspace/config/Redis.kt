package com.ideaspace.config

import com.ideaspace.document.DocumentRedisPublisher
import com.ideaspace.workers.RedisManager
import io.ktor.server.application.*
import io.ktor.server.plugins.di.dependencies
import kotlinx.coroutines.runBlocking


fun Application.configureRedisRoute(){
    monitor.subscribe(ApplicationStarted) {
        RedisManager.connection
    }

    monitor.subscribe(ApplicationStopping) {
        runBlocking {
            RedisManager.close()
        }
    }

    dependencies.provide{
        DocumentRedisPublisher()
    }
}