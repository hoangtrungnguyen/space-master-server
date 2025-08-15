package com.ideaspace.config

import com.ideaspace.core.datasources.redis.RedisPlugin
import com.ideaspace.core.datasources.redis.redisPoolKey
import io.ktor.server.application.Application
import io.ktor.server.application.install
import io.ktor.server.plugins.di.dependencies

fun Application.configureRedis(){

    install(RedisPlugin)

    val jedisPool = attributes[redisPoolKey]



}