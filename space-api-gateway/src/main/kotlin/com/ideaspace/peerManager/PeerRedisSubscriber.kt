package com.ideaspace.peerManager

import com.ideaspace.core.dto.UUIDToString
import com.ideaspace.core.redis.RedisManager
import io.lettuce.core.RedisClient
import io.lettuce.core.pubsub.RedisPubSubListener
import io.lettuce.core.pubsub.StatefulRedisPubSubConnection
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.consumeEach
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.util.*
import java.util.concurrent.ConcurrentHashMap

@Serializable
data class PeerEvent(
    val type: PeerEventType,
    @Serializable(with = com.ideaspace.core.dto.UUIDToString::class)
    val peerUuid: UUID,
    val docId: Long
)

enum class PeerEventType { JOIN, LEAVE }

class PeerRedisSubscriber(
    private val redisClient: RedisClient,
    private val coroutineScope: CoroutineScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
) {
    private val subscriptions = ConcurrentHashMap<Long, PeerGroupSubscription>()
    
    // Separate PubSub connection
    private val pubSubConnection: StatefulRedisPubSubConnection<String, String> by lazy {
        redisClient.connectPubSub()
    }
    
    // Standard connection for commands (SADD, SREM, SMEMBERS)
    private val commands = RedisManager.connectionString.sync()

    private data class PeerGroupSubscription(
        val docId: Long,
        val channel: Channel<PeerEvent>,
        val job: Job
    )

    private fun getChannelName(docId: Long) = "peer:events:$docId"
    private fun getSetKey(docId: Long) = "peer:set:$docId"

    suspend fun subscribeToPeerGroup(docId: Long, onEvent: suspend (PeerEvent) -> Unit) {
        if (subscriptions.containsKey(docId)) return

        val eventChannel = Channel<PeerEvent>(Channel.UNLIMITED)
        val redisChannelName = getChannelName(docId)

        val job = coroutineScope.launch {
            eventChannel.consumeEach { event ->
                try {
                    onEvent(event)
                } catch (e: Exception) {
                    println("Error processing peer event: ${e.message}")
                }
            }
        }

        subscriptions[docId] = PeerGroupSubscription(docId, eventChannel, job)

        // Add Listener
        pubSubConnection.addListener(object : RedisPubSubListener<String, String> {
            override fun message(channel: String, message: String) {
                if (channel == redisChannelName) {
                    try {
                        val event = Json.decodeFromString<PeerEvent>(message)
                        subscriptions[docId]?.channel?.trySend(event)
                    } catch (e: Exception) {
                        println("Error decoding peer message: $message")
                    }
                }
            }
            override fun message(pattern: String, channel: String, message: String) {}
            override fun subscribed(channel: String, count: Long) {}
            override fun psubscribed(pattern: String, count: Long) {}
            override fun unsubscribed(channel: String, count: Long) {}
            override fun punsubscribed(pattern: String, count: Long) {}
        })

        pubSubConnection.sync().subscribe(redisChannelName)
        println("[PeerRedisSubscriber] Subscribed to $redisChannelName")
    }

    suspend fun unsubscribeFromPeerGroup(docId: Long) {
        subscriptions.remove(docId)?.let { sub ->
            pubSubConnection.sync().unsubscribe(getChannelName(docId))
            sub.channel.close()
            sub.job.cancel()
            println("[PeerRedisSubscriber] Unsubscribed from ${getChannelName(docId)}")
        }
    }

    fun joinAndNotify(docId: Long, peerUuid: UUID): List<UUID> {
        // 1. Add to Redis Set
        commands.sadd(getSetKey(docId), peerUuid.toString())
        
        // 2. Publish JOIN event
        val event = PeerEvent(PeerEventType.JOIN, peerUuid, docId)
        commands.publish(getChannelName(docId), Json.encodeToString(event))

        // 3. Return current list of peers (snapshot)
        val members = commands.smembers(getSetKey(docId))
        return members.mapNotNull { 
            try { UUID.fromString(it) } catch (e: Exception) { null } 
        }
    }

    fun leaveAndNotify(docId: Long, peerUuid: UUID) {
        // 1. Remove from Redis Set
        commands.srem(getSetKey(docId), peerUuid.toString())

        // 2. Publish LEAVE event
        val event = PeerEvent(PeerEventType.LEAVE, peerUuid, docId)
        commands.publish(getChannelName(docId), Json.encodeToString(event))
    }

    fun getPeers(docId: Long): List<UUID> {
        return commands.smembers(getSetKey(docId)).mapNotNull {
            try { UUID.fromString(it) } catch (e: Exception) { null }
        }
    }

    fun close() {
        subscriptions.values.forEach { 
            it.channel.close()
            it.job.cancel() 
        }
        subscriptions.clear()
        pubSubConnection.close()
        // We do not close data connection here as it might be shared or managed by RedisManager
        // But in this class we access it via RedisManager.connectionString which is static/singleton
        coroutineScope.cancel()
        println("[PeerRedisSubscriber] Closed all subscriptions")
    }
}