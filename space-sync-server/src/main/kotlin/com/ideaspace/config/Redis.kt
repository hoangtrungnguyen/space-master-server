package com.ideaspace.config

import com.ideaspace.core.datasources.redis.RedisPlugin
import com.ideaspace.core.datasources.redis.redisPoolKey
import com.ideaspace.workers.RedisPublisher
import io.ktor.server.application.*
import io.ktor.server.engine.EmbeddedServer
import io.ktor.server.plugins.di.dependencies
import io.ktor.server.plugins.di.provide
import io.ktor.server.request.receiveText
import io.ktor.server.response.respondText
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import io.ktor.server.routing.routing


fun Application.configureRedisRoute(){

    install(RedisPlugin)

    val jedisPool = attributes[redisPoolKey]

    dependencies.provide<RedisPublisher> {
        RedisPublisher(jedisPool)
    }

}