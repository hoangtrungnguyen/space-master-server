package com.ideaspace.workers

import com.ideaspace.core.redis.EditDocEvent
import com.ideaspace.core.redis.FinishSyncEvent
import com.ideaspace.core.redis.InitSyncEvent
import com.ideaspace.core.redis.SaveDocEvent
import com.ideaspace.core.redis.redisDocProcessKey
import io.lettuce.core.RedisClient
import io.lettuce.core.api.StatefulRedisConnection
import org.slf4j.LoggerFactory


object RedisManager {
    // Initialize the client (points to your Redis server)
    private val client: RedisClient = RedisClient.create("redis://localhost:6379")

    // Create a reusable connection
    val connection: StatefulRedisConnection<String, String> = client.connect()

    fun close() {
        connection.close()
        client.shutdown()
    }
}