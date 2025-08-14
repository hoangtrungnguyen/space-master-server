package com.ideaspace.config

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

fun Application.configureRedisRoute(){

    install(RedisPlugin)

    val jedisPool = attributes[redisPoolKey]

    dependencies.provide<RedisPublisher> {
        RedisPublisher(jedisPool)
    }

    routing {
        // Example route to cache data for a user session
        post("/cache/user/{userId}") {
            val userId = call.parameters["userId"] ?: return@post call.respondText("Missing user ID")
            val sessionData = call.receiveText() // e.g., JSON string of user's board state

            // Get a Jedis instance from the pool
            jedisPool.resource.use { jedis ->
                // Set a key with an expiration of 1 hour (3600 seconds)
                jedis.setex("user:session:$userId", 3600, sessionData)
            }

            call.respondText("Cached session for user $userId")
        }

        // Example route to retrieve cached data
        get("/cache/user/{userId}") {
            val userId = call.parameters["userId"] ?: return@get call.respondText("Missing user ID")
            var response: String?

            jedisPool.resource.use { jedis ->
                response = jedis.get("user:session:$userId")
            }

            if (response != null) {
                call.respondText(response)
            } else {
                call.respondText("No session found for user $userId", status = io.ktor.http.HttpStatusCode.NotFound)
            }
        }

        post("/cache/documents/{processId}"){

        }
    }

}