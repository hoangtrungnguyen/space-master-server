@file:OptIn(ExperimentalSerializationApi::class)

package com.ideaspace.session

import com.ideaspace.core.redis.redisDocKeyPattern
import com.ideaspace.core.redis.redisDocSyncEventsKey
import com.ideaspace.core.redis.toLong
import io.lettuce.core.Limit
import io.lettuce.core.Range.unbounded
import io.lettuce.core.api.StatefulRedisConnection
import io.lettuce.core.pubsub.RedisPubSubListener
import io.lettuce.core.pubsub.StatefulRedisPubSubConnection
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.consumeEach
import kotlinx.serialization.ExperimentalSerializationApi
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.util.concurrent.ConcurrentHashMap

val logger: Logger = LoggerFactory.getLogger("RedisSubscriber")

/**
 * Redis keyspace notification subscriber for document sync events.
 * Listens for events on keys matching the pattern: ideaspace:doc:<docId>:sync-events
 */
class RedisSubscriber(
    private val redis: StatefulRedisConnection<String, ByteArray>,
    private val redisPubSub: StatefulRedisPubSubConnection<String, String>
) {

    private val coroutineScope: CoroutineScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private val subscriptions = ConcurrentHashMap<Long, DocumentSubscription>()

    init {
        redis.async().configGet("notify-keyspace-events").thenAccept { result ->
            val keyspaceConf = result.get("notify-keyspace-events") ?: ""
            if (keyspaceConf.contains('K') && keyspaceConf.contains('t')) {
                logger.info("Keyspace events (K) are available for stream commands (t). " +
                        "Current 'notify-keyspace-events'='$keyspaceConf'.")
            } else {
                logger.error("Keyspace events (K) are NOT available for stream commands (t). " +
                        "Current 'notify-keyspace-events'='$keyspaceConf'.")
            }
        }
    }

    /**
     * Data class to hold subscription information for a document
     */
    private data class DocumentSubscription(
        val docId: Long,
        val channel: Channel<String>,
        val job: Job
    )

    /**
     * Subscribe to keyspace notifications for a specific document
     * @param docId The document ID to subscribe to
     * @param onMessage Callback function to handle sync events
     */
    suspend fun subscribeToDocument(docId: Long, onMessage: suspend (StreamAddEntry) -> Unit) {
        // Check if already subscribed
        if (subscriptions.containsKey(docId)) {
            println("Already subscribed to document $docId")
            return
        }

        val streamKey = redisDocSyncEventsKey(docId)
        val streamPattern = redisDocKeyPattern(docId)
        val redisEventChannel = Channel<String>(Channel.UNLIMITED)
        
        // Create a job to process events for this document
        val job = coroutineScope.launch {
            // TODO Wait for maximum 200ms between sends.
            //      If reached 200 messages before 200ms from the last send, then send immediately and reset the clock


            redisEventChannel.consumeEach { redisEvent ->
                try {
                    if (redisEvent == "xadd") {
                        // Query for the latest stream entry-id.
                        // Then we can craft a meaningful message for the subscribers
                        val entries = redis.sync().xrevrange(streamKey, unbounded(), Limit.from(1))
                        val latestEntry = entries.firstOrNull()
                        val sourceProcessId = latestEntry?.body?.get("sourceProcessId")?.toLong()
                        if (sourceProcessId != null) {
                            onMessage(StreamAddEntry(entryId = latestEntry.id, sourceProcessId = sourceProcessId))
                        }
                    }
                } catch (e: Exception) {
                    println("Error consuming redis event '$redisEvent' for document '$docId': ${e.message}")
                }
            }
        }

        subscriptions[docId] = DocumentSubscription(docId, redisEventChannel, job)

        // Set up the Redis pub/sub listener
        redisPubSub.addListener(object : RedisPubSubListener<String, String> {
            override fun message(channel: String, message: String) {
                if (channel == streamPattern) {
                    subscriptions[docId]?.channel?.trySend(message)
                }
            }

            override fun message(pattern: String, channel: String, message: String) {
                if (pattern == streamPattern) {
                    subscriptions[docId]?.channel?.trySend(message)
                }
            }

            override fun subscribed(channel: String, count: Long) {
                println("✅ Subscribed to Redis keyspace notifications for document $docId on channel: $channel")
            }

            override fun psubscribed(pattern: String, count: Long) {
                println("✅ Pattern subscribed to Redis keyspace notifications for document $docId on pattern: $pattern")
            }

            override fun unsubscribed(channel: String, count: Long) {
                println("❌ Unsubscribed from Redis keyspace notifications for document $docId on channel: $channel")
            }

            override fun punsubscribed(pattern: String, count: Long) {
                println("❌ Pattern unsubscribed from Redis keyspace notifications for document $docId on pattern: $pattern")
            }
        })

        // Subscribe to the specific key pattern
        redisPubSub.sync().subscribe(streamPattern)

        println("[RedisSubscriber] - subscriptions - size ${subscriptions.size}")
        println("🔔 Started Redis keyspace notification subscription for document $docId")
    }

    /**
     * Unsubscribe from keyspace notifications for a specific document
     * @param docId The document ID to unsubscribe from
     */
    suspend fun unsubscribeFromDocument(docId: Long) {
        val subscription = subscriptions.remove(docId)
        if (subscription != null) {
            val streamPattern = "__keyspace@0__:${redisDocSyncEventsKey(docId)}"

            // Unsubscribe from Redis
            redisPubSub.sync().unsubscribe(streamPattern)
            
            // Close the channel and cancel the job
            subscription.channel.close()
            subscription.job.cancel()
            
            println("🔕 Stopped Redis keyspace notification subscription for document $docId")
        }
    }

    /**
     * Check if subscribed to a document
     */
    fun isSubscribed(docId: Long): Boolean {
        return subscriptions.containsKey(docId)
    }

    /**
     * Get the count of active subscriptions
     */
    fun getSubscriptionCount(): Int {
        return subscriptions.size
    }

    /**
     * Close all subscriptions and cleanup resources
     */
    suspend fun close() {
        // Cancel all subscription jobs and close channels
        subscriptions.values.forEach { subscription ->
            subscription.channel.close()
            subscription.job.cancel()
        }
        subscriptions.clear()

        redis.close()
        redisPubSub.close()
        
        // Cancel the coroutine scope
        coroutineScope.cancel()
        
        println("🔐 Closed all Redis keyspace notification subscriptions")
    }
}
