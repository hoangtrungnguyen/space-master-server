package com.ideaspace.document

import com.ideaspace.core.kafkaMessage.EditElementEventValue
import com.ideaspace.core.redis.StringByteArrayCodec
import io.lettuce.core.RedisClient
import io.lettuce.core.api.StatefulRedisConnection
import io.lettuce.core.api.sync.RedisCommands
import io.lettuce.core.pubsub.RedisPubSubListener
import io.lettuce.core.pubsub.StatefulRedisPubSubConnection
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertArrayEquals
import org.junit.jupiter.api.BeforeEach
import java.util.*
import java.util.concurrent.CompletableFuture
import java.util.concurrent.TimeUnit
import kotlin.test.Test

class DocumentRedisPublisherTest {

    private lateinit var redisClient: RedisClient
    private lateinit var subscriberConnection: StatefulRedisPubSubConnection<String, ByteArray>
    private lateinit var byteArraySubscriberConnection: StatefulRedisPubSubConnection<ByteArray, ByteArray>
    private lateinit var publisherConnection: StatefulRedisConnection<String, ByteArray>

    @BeforeEach
    fun setup() {
        redisClient = RedisClient.create("redis://localhost:6379")

        publisherConnection = redisClient.connect(StringByteArrayCodec())

        subscriberConnection = redisClient.connectPubSub(StringByteArrayCodec())


        // Ensure keyspace notifications are enabled using a string command connection
        redisClient.connect().use {
            it.sync().configSet("notify-keyspace-events", "K\$t")
        }
    }

    @AfterEach
    fun tearDown() {
        publisherConnection.close()
        subscriberConnection.close()
        redisClient.shutdown()
    }

    @Test
    fun `listen to xadd`() {
        val docId = 12L
        val editElementEventValue1 = EditElementEventValue(
            docId = 1L,
            processId = 101L,
            userId = 123L,
            windowId = 456L,
            uuid = UUID.fromString("a1b2c3d4-e5f6-7890-1234-567890abcdef"),
            metadata = buildJsonObject {
                put("updatedBy", "user1")
                put("timestamp", "2023-10-27T10:00:00Z")
            },
            type = "rectangle",
            value = buildJsonObject {
                put("x", 100)
                put("y", 200)
                put("width", 150)
                put("height", 75)
                put("fill", "#FF0000")
            }
        )

        // Define keys and channels as byte arrays
        val streamKey = "test:doc:stream:bytes"
        val notificationChannel = "__keyspace@0__:$streamKey"

        val futureNotification = CompletableFuture<ByteArray>()

        subscriberConnection.addListener(
            StringKeyStringValueListener(
                notificationChannel,
                futureNotification,
            )
        )

        val connection = redisClient.connect(StringByteArrayCodec())

        subscriberConnection.sync().subscribe(notificationChannel)

        val syncCommands: RedisCommands<String, ByteArray> = connection.sync()

        syncCommands.startXAdd(
            editElementEventValue1,
            streamKey,
            -3L
        )

//        syncCommands.xadd(
//            notificationChannel,
//            mapOf<String, ByteArray>(
//                "sourceProcessId" to 12324L.toBytes(),
//                "bytes" to 1233L.toBytes(),
//            )
//        )

        val expectedMessage = "xadd".toByteArray()
        val receivedMessage = futureNotification.get(3, TimeUnit.SECONDS)

        assertArrayEquals(expectedMessage, receivedMessage)
    }


    inner class ArrayByteChannelArrayByteValueListener(
        val notificationChannel: ByteArray,
        val futureNotification: CompletableFuture<ByteArray>
    ) : RedisPubSubListener<ByteArray, ByteArray> {
        override fun message(channel: ByteArray?, message: ByteArray?) {
            println("Message by channel: ${channel} ~ message: ${message?.decodeToString()}")
            futureNotification.complete(message)
        }

        override fun message(pattern: ByteArray?, channel: ByteArray?, message: ByteArray?) {
        }

        override fun subscribed(channel: ByteArray?, count: Long) {
        }

        override fun psubscribed(pattern: ByteArray?, count: Long) {
        }

        override fun unsubscribed(channel: ByteArray?, count: Long) {
        }

        override fun punsubscribed(pattern: ByteArray?, count: Long) {
        }

    }

    inner class StringKeyStringValueListener(
        val notificationChannel: String,
        val futureNotification: CompletableFuture<ByteArray>,
        val docId: Long = -123L
    ) : RedisPubSubListener<String, ByteArray> {
        override fun message(channel: String, message: ByteArray) {
            println("Message by channel: ${channel} ~ message: ${message?.decodeToString()}")
//            if (channel.contentEquals(notificationChannel)) {
            futureNotification.complete(message)
//            }
        }

        override fun message(pattern: String, channel: String, message: ByteArray) {

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
    }

}