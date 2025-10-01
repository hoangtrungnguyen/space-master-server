import io.lettuce.core.api.sync.RedisCommands
import io.lettuce.core.pubsub.StatefulRedisPubSubConnection


class ApiGatewayRedisListenerTest {

    private lateinit var subscriberConnection: StatefulRedisPubSubConnection<String, ByteArray>

    fun setUp() {
        val command: RedisCommands<String, ByteArray> = subscriberConnection.sync()
    }
}