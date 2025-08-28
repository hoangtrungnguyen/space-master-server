package com.ideaspace.core.redis

import io.lettuce.core.RedisClient
import io.lettuce.core.api.StatefulRedisConnection


object RedisManager {
    // Initialize the client (points to your Redis server)
    private val client: RedisClient = RedisClient.create("redis://localhost:6379")

    // Create a reusable connection
    val connection: StatefulRedisConnection<String, ByteArray> = client.connect(StringByteArrayCodec())

    val connectionString: StatefulRedisConnection<String, String> = client.connect()

    fun close() {
        connection.close()
        connectionString.close()
        client.shutdown()
    }
}
