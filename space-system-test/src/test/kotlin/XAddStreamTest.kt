import com.ideaspace.core.redis.StringByteArrayCodec
import com.ideaspace.core.redis.toBytes
import io.lettuce.core.RedisClient
import io.lettuce.core.api.StatefulRedisConnection
import io.lettuce.core.api.sync.RedisCommands
import io.lettuce.core.codec.ByteArrayCodec
import io.lettuce.core.pubsub.RedisPubSubListener
import io.lettuce.core.pubsub.StatefulRedisPubSubConnection
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertArrayEquals
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import java.time.Clock
import java.util.concurrent.CompletableFuture
import java.util.concurrent.TimeUnit

class RedisListenerTest {
    @Nested
    inner class RedisNotificationTest {

        private lateinit var redisClient: RedisClient
        private lateinit var publisherConnection: StatefulRedisConnection<String, String>
        private lateinit var subscriberConnection: StatefulRedisPubSubConnection<String, String>

        @BeforeEach
        fun setup() {
            // 1. Initialize connections
            redisClient = RedisClient.create("redis://localhost:6379")
            publisherConnection = redisClient.connect()
            subscriberConnection = redisClient.connectPubSub()

            // 2. IMPORTANT: Ensure keyspace notifications are enabled for the test
            // This makes the test self-contained and not reliant on server config.
            publisherConnection.sync().configSet("notify-keyspace-events", "K\$t") // K=Keyspace, $=String, t=Stream
        }

        @AfterEach
        fun tearDown() {
            publisherConnection.close()
            subscriberConnection.close()
            redisClient.shutdown()
        }

        @Test
        fun `when xadd is used, a keyspace notification should be published`() {
            val streamKey = "ideaspace:doc:6:stream"
            val notificationChannel = "__keyspace@0__:$streamKey"

            // Use CompletableFuture to wait for the async notification
            val futureNotification = CompletableFuture<String>()

            // === Step 1: Set up the subscriber ===
            val subscriberCommands = subscriberConnection.sync()
            subscriberConnection.addListener(object : RedisPubSubListener<String, String> {
                override fun message(channel: String?, message: String?) {
                    println("Message by channel: ${channel} ~ message: ${message}")

                    futureNotification.complete(message)
                }

                override fun message(pattern: String?, channel: String?, message: String?) {
                }

                override fun subscribed(channel: String?, count: Long) {
                }

                override fun psubscribed(pattern: String?, count: Long) {
                }

                override fun unsubscribed(channel: String?, count: Long) {
                }

                override fun punsubscribed(pattern: String?, count: Long) {
                }

            })
            subscriberCommands.subscribe(notificationChannel)

            // === Step 2: Trigger the event with the publisher ===
            val publisherCommands = publisherConnection.sync()
            publisherCommands.xadd(streamKey, mapOf("event" to "login", "userId" to "123"))

            // === Step 3: Wait for the notification and assert the result ===
            // The message payload for a keyspace event is the command that was executed.
            val expectedMessage = "xadd"
            val receivedMessage = futureNotification.get(5, TimeUnit.SECONDS) // Wait up to 5s

            assertEquals(expectedMessage, receivedMessage)
        }

    }

    @Nested
    inner class RedisByteArrayNotificationTest {

        private lateinit var redisClient: RedisClient
        private lateinit var publisherConnection: StatefulRedisConnection<ByteArray, ByteArray>
        private lateinit var subscriberConnection: StatefulRedisPubSubConnection<ByteArray, ByteArray>

        @BeforeEach
        fun setup() {
            redisClient = RedisClient.create("redis://localhost:6379")

            // Connect using ByteArrayCodec for both connections
            publisherConnection = redisClient.connect(ByteArrayCodec.INSTANCE)
            subscriberConnection = redisClient.connectPubSub(ByteArrayCodec.INSTANCE)

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
        fun `when xadd is used with byte array data, a notification should be published`() {
            // Define keys and channels as byte arrays
            val streamKey = "test:user:stream:bytes".toByteArray()
            val notificationChannel = "__keyspace@0__:".toByteArray() + streamKey

            // The future now holds a ByteArray
            val futureNotification = CompletableFuture<ByteArray>()

            // === Step 1: Set up the subscriber ===
            val addListener = subscriberConnection.addListener(object : RedisPubSubListener<ByteArray, ByteArray> {
                override fun message(channel: ByteArray?, message: ByteArray?) {
                    println("Message by channel: ${channel?.decodeToString()} ~ message: ${message?.decodeToString()}")
                    if (channel.contentEquals(notificationChannel)) {
                        futureNotification.complete(message)
                    }
                }

                override fun message(pattern: ByteArray?, channel: ByteArray?, message: ByteArray?) {
                    println("Message by Pattern: $channel . message: $message")
                }

                override fun subscribed(channel: ByteArray?, count: Long) {
                }

                override fun psubscribed(pattern: ByteArray?, count: Long) {
                }

                override fun unsubscribed(channel: ByteArray?, count: Long) {
                }

                override fun punsubscribed(pattern: ByteArray?, count: Long) {
                }

            })
            subscriberConnection.sync().subscribe(notificationChannel)

            // === Step 2: Trigger the event with byte array data ===
            val publisherCommands = publisherConnection.sync()

            for (i in 0..5) {
                val eventData = mapOf(
                    "event".toByteArray() to "login $i".toByteArray(),
                    "userId".toByteArray() to "user-123-bytes ${Clock.systemUTC()}".toByteArray()
                )
                publisherCommands.xadd(streamKey, eventData)
            }

            // === Step 3: Wait and assert the byte array result ===
            val expectedMessage = "xadd".toByteArray()
            val receivedMessage = futureNotification.get(5, TimeUnit.SECONDS)

            // Use assertArrayEquals for comparing byte arrays
            assertArrayEquals(expectedMessage, receivedMessage)
        }
    }


    @Nested
    inner class RedisStringKeyToBytesNotification {

        private lateinit var redisClient: RedisClient
        private lateinit var publisherConnection: StatefulRedisConnection<String, ByteArray>
        private lateinit var subscriberConnection: StatefulRedisPubSubConnection<String, ByteArray>

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
        fun `when xadd is used with byte array data, a notification should be published`() {
            // Define keys and channels as byte arrays
            val streamKey = "test:integration:stream:bytes"
            val notificationChannel = "__keyspace@0__:$streamKey"

            // The future now holds a ByteArray
            val futureNotification = CompletableFuture<ByteArray>()

            // === Step 1: Set up the subscriber ===
            subscriberConnection.addListener(object : RedisPubSubListener<String, ByteArray> {
                override fun message(channel: String, message: ByteArray?) {
                    println("Message by channel: ${channel} ~ message: ${message?.decodeToString()}")
                    if (channel.contentEquals(notificationChannel)) {
                        futureNotification.complete(message)
                    }
                }

                override fun message(pattern: String?, channel: String?, message: ByteArray?) {
                    println("Message by Pattern: $channel . message: $message")
                    futureNotification.complete(message)

                }

                override fun subscribed(channel: String?, count: Long) {
                }

                override fun psubscribed(pattern: String?, count: Long) {
                }

                override fun unsubscribed(channel: String?, count: Long) {
                }

                override fun punsubscribed(pattern: String?, count: Long) {
                }

            })
            subscriberConnection.sync().subscribe(notificationChannel)

            // === Step 2: Trigger the event with byte array data ===
            val syncCommands: RedisCommands<String, ByteArray> = publisherConnection.sync()

            for (i in 0..5) {
                syncCommands.xadd(
                    streamKey,
                    mapOf<String, ByteArray>(
                        "sourceProcessId" to 12324L.toBytes(),
                        "bytes" to 1233L.toBytes(),
                    )
                )
            }

            // === Step 3: Wait and assert the byte array result ===
            val expectedMessage = "xadd".toByteArray()
            val receivedMessage = futureNotification.get(5, TimeUnit.SECONDS)

            // Use assertArrayEquals for comparing byte arrays
            assertArrayEquals(expectedMessage, receivedMessage)
        }
    }
}