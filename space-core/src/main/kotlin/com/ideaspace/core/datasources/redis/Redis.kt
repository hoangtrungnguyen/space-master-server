package com.ideaspace.core.datasources.redis

import io.ktor.server.application.ApplicationStopping
import io.ktor.server.application.createApplicationPlugin
import io.ktor.server.application.log
import redis.clients.jedis.JedisPool
import redis.clients.jedis.JedisPoolConfig

// Custom Ktor Plugin for Jedis
val RedisPlugin = createApplicationPlugin(name = "RedisPlugin") {
    // Get Redis configuration from application.conf
    val host = application.environment.config.property("redis.host").getString()
    val port = application.environment.config.property("redis.port").getString().toInt()

    // Configure and create the connection pool
    val jedisPool = JedisPool(JedisPoolConfig(), host, port)

    // Make the pool available to the application
    application.attributes.put(redisPoolKey, jedisPool)

    // Add a shutdown hook to close the pool when the application stops
    application.monitor.subscribe(ApplicationStopping) {
        application.log.info("Closing Redis connection pool.")
        jedisPool.close()
    }

}

// A key to access the JedisPool from the application instance
val redisPoolKey = io.ktor.util.AttributeKey<JedisPool>("redisPool")