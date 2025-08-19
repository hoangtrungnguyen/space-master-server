
package com.ideaspace.session

import com.ideaspace.core.redis.redisDocSyncEventsKey
import io.lettuce.core.Limit
import io.lettuce.core.Range.unbounded
import io.lettuce.core.RedisClient
import io.lettuce.core.api.StatefulRedisConnection
import io.lettuce.core.pubsub.RedisPubSubListener
import io.lettuce.core.pubsub.StatefulRedisPubSubConnection
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.consumeEach
import java.util.concurrent.ConcurrentHashMap

/**
 * Redis keyspace notification subscriber for document sync events.
 * Listens for events on keys matching the pattern: ideaspace:doc:<docId>:sync-events
 */
class RedisSubscriber(
    private val redisClient: RedisClient,
    private val coroutineScope: CoroutineScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
) {

    private val subscriptions = ConcurrentHashMap<Long, DocumentSubscription>()
    private val dataConnection: StatefulRedisConnection<String, String> by lazy {
        redisClient.connect()
    }
    private val pubSubConnection: StatefulRedisPubSubConnection<String, String> by lazy {
        redisClient.connectPubSub()
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
    suspend fun subscribeToDocument(docId: Long, onMessage: suspend (DocumentMessage) -> Unit) {
        // Check if already subscribed
        if (subscriptions.containsKey(docId)) {
            println("Already subscribed to document $docId")
            return
        }

        val streamKey = redisDocSyncEventsKey(docId)
        val streamPattern = "__keyspace@0__:${streamKey}"
        val redisEventChannel = Channel<String>(Channel.UNLIMITED)
        
        // Create a job to process events for this document
        val job = coroutineScope.launch {
            redisEventChannel.consumeEach { redisEvent ->
                try {
                    if (redisEvent == "xadd") {
                        // Query for the latest stream entry-id.
                        // Then we can craft a meaningful message for the subscribers
                        val entries = dataConnection.sync().xrevrange(streamKey, unbounded(), Limit.from(1))
                        val latestEntry = entries.firstOrNull()
                        if (latestEntry != null) {
                            onMessage(StreamAddEntry(latestEntry.id))
                        } else {

                        }
                    }
                } catch (e: Exception) {
                    println("Error consuming redis event '$redisEvent' for document '$docId': ${e.message}")
                }
            }
        }

        subscriptions[docId] = DocumentSubscription(docId, redisEventChannel, job)

        // Set up the Redis pub/sub listener
        pubSubConnection.addListener(object : RedisPubSubListener<String, String> {
            override fun message(channel: String, message: String) {
                if (channel == streamPattern) {
                    // Send the message to the document's channel
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
        pubSubConnection.sync().subscribe(streamPattern)
        
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
            pubSubConnection.sync().unsubscribe(streamPattern)
            
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

        dataConnection.close()
        pubSubConnection.close()
        
        // Cancel the coroutine scope
        coroutineScope.cancel()
        
        println("🔐 Closed all Redis keyspace notification subscriptions")
    }
}
