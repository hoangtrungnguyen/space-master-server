package com.ideaspace.peerManager

import com.ideaspace.core.redis.RedisManager
import com.ideaspace.core.redis.redisDocSyncEventsKey
import com.ideaspace.core.redis.redisPeer2PeerEventsKey
import com.ideaspace.session.ListPeerOut
import com.ideaspace.session.logger
import io.lettuce.core.RedisClient
import io.lettuce.core.api.StatefulRedisConnection
import io.lettuce.core.api.sync.RedisCommands
import io.lettuce.core.pubsub.RedisPubSubListener
import io.lettuce.core.pubsub.StatefulRedisPubSubConnection
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.consumeEach
import kotlinx.serialization.json.*
import java.util.concurrent.ConcurrentHashMap


class PeerRedisSubscriber(
    private val redisClient: RedisClient,
    private val coroutineScope: CoroutineScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
) {

    private val subscriptions = ConcurrentHashMap<Long, PeerGroupSubscription>()

    private val dataConnection: StatefulRedisConnection<String, String> by lazy {
        redisClient.connect()
    }
    private val pubSubConnection: StatefulRedisPubSubConnection<String, String> by lazy {
        redisClient.connectPubSub()
    }

    init {
        dataConnection.async().configGet("peer-keyspace-events").thenAccept { result ->
            val keyspaceConf = result.get("notify-keyspace-events") ?: ""
            if (keyspaceConf.contains('K') && keyspaceConf.contains('t')) {
                logger.info(
                    "Keyspace events (K) are available for stream commands (t). " +
                            "Current 'notify-keyspace-events'='$keyspaceConf'."
                )
            } else {
                logger.error(
                    "Keyspace events (K) are NOT available for stream commands (t). " +
                            "Current 'notify-keyspace-events'='$keyspaceConf'."
                )
            }
        }
    }

    private data class PeerGroupSubscription(
        val docId: Long,
        val channel: Channel<ListPeerOut>,
        val job: Job
    )

    suspend fun subscribeToPeerGroup(docId: Long, onMessage: suspend (ListPeerOut) -> Unit) {
        // Check if already subscribed
        if (subscriptions.containsKey(docId)) {
            println("Already subscribed to document $docId")
            return
        }

        val streamKey = redisPeer2PeerEventsKey(docId)
        val streamPattern = "__keyspace@0__:${streamKey}"
        val redisEventChannel = Channel<ListPeerOut>(Channel.UNLIMITED)

        val job = coroutineScope.launch {
            redisEventChannel.consumeEach { redisEvent ->
                try {
                    println("[PeerRedisSubscriber] event received")
                    onMessage(redisEvent)
                } catch (e: Exception) {
                    println("Error consuming redis event '$redisEvent' for document '$docId': ${e.message}")
                }
            }
        }

        subscriptions[docId] = PeerGroupSubscription(docId, redisEventChannel, job)

        pubSubConnection.addListener(object : RedisPubSubListener<String, String> {
            override fun message(channel: String, message: String) {
                if (channel == streamPattern) {
                    try {
                        val listPeer = Json.decodeFromString<ListPeerOut>(message)
                        subscriptions[docId]?.channel?.trySend(listPeer)
                    } catch (e: Exception) {
                        println("[PeerRedisSubscriber] Error sending message to $message")
                    }
                }
            }

            override fun message(pattern: String, channel: String, message: String) {
                if (channel == streamPattern) {
                    try {
                        val listPeer = Json.decodeFromString<ListPeerOut>(message)
                        subscriptions[docId]?.channel?.trySend(listPeer)
                    } catch (e: Exception) {
                        println("[PeerRedisSubscriber] Error sending message to $message")
                    }
                }
            }

            override fun subscribed(channel: String, count: Long) {
                println("[PeerRedisSubscriber] ✅ Subscribed to Redis keyspace doc:$docId on channel:$channel")
            }

            override fun psubscribed(pattern: String, count: Long) {
                println("[PeerRedisSubscriber] ✅ Pattern subscribed to Redis keyspace  doc:$docId on pattern:$pattern")
            }

            override fun unsubscribed(channel: String, count: Long) {
                println("[PeerRedisSubscriber] ❌ Unsubscribed from Redis keyspace document $docId on channel: $channel")
            }

            override fun punsubscribed(pattern: String, count: Long) {
                println("[PeerRedisSubscriber] ❌ Pattern unsubscribed from Redis keyspace document $docId on pattern: $pattern")
            }
        })

        pubSubConnection.sync().subscribe(streamPattern)

        println("[PeerRedisSubscriber] 🔔 Started Redis keyspace notification subscription for document $docId")

    }

    suspend fun unsubscribeFromPeerGroup(docId: Long) {
        val subscription = subscriptions.remove(docId)
        if (subscription != null) {
            val streamPattern = "__keyspace@0__:${redisDocSyncEventsKey(docId)}"

            // Unsubscribe from Redis
            pubSubConnection.sync().unsubscribe(streamPattern)

            // Close the channel and cancel the job
            subscription.channel.close()

            println("[PeerRedisSubscriber] 🔕 Stopped Redis keyspace notification subscription for document $docId")
        }
    }

    suspend fun publishListPeerEvent(
        docId: Long,
        listPeerOut: ListPeerOut
    ): String {
        val streamKey = redisPeer2PeerEventsKey(docId)
        val streamPattern = "__keyspace@0__:${streamKey}"
        val syncCommands: RedisCommands<String, String> = RedisManager.connection.sync()
        return syncCommands.startXAdd(listPeerOut, streamPattern)
    }


}

private fun RedisCommands<String, String>.startXAdd(
    listPeerOut: ListPeerOut,
    redisKey: String,
): String {
    val jsonElement = Json.encodeToJsonElement(listPeerOut)
    if (jsonElement is JsonObject) {
        val redisMap: Map<String, String> = jsonElement.jsonObject.mapValues { (_, value) ->
            if (value is JsonPrimitive) {
                value.content
            } else {
                Json.encodeToString(JsonElement.serializer(), value)
            }
        }
        val messageId = this.xadd(redisKey, redisMap)
        return messageId.also {
            println("✅ Send peer data'$redisKey' with message ID $messageId")
        }
    } else {
        throw Exception("The provided event did not serialize to a JSON object, cannot publish to Redis stream.")

    }
}