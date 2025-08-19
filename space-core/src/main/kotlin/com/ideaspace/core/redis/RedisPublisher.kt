package com.ideaspace.core.redis

import io.lettuce.core.RedisClient
import io.lettuce.core.api.StatefulRedisConnection


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

/**
 * Redis publisher for document sync events
 */
object RedisSyncEventPublisher {
    
    /**
     * Publish a sync event to the document sync events key
     * This will trigger keyspace notifications for subscribers
     * @param docId The document ID
     * @param syncEvent The sync event data to publish
     */
    fun publishSyncEvent(docId: Long, syncEvent: String) {
        val syncEventsKey = redisDocSyncEventsKey(docId)
        val syncCommands = RedisManager.connection.sync()
        
        try {
            // Publish to the sync events key - this will trigger keyspace notifications
            syncCommands.publish(syncEventsKey, syncEvent)
            println("✅ Published sync event to Redis key '$syncEventsKey': $syncEvent")
        } catch (e: Exception) {
            println("❌ Failed to publish sync event to Redis: ${e.message}")
        }
    }
}