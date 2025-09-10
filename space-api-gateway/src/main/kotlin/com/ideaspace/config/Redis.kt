package com.ideaspace.config

import com.ideaspace.core.redis.StringByteArrayCodec
import com.ideaspace.session.RedisSubscriber
import io.ktor.server.application.*
import io.ktor.server.plugins.di.*
import io.lettuce.core.RedisClient
import io.lettuce.core.api.StatefulRedisConnection
import io.lettuce.core.pubsub.StatefulRedisPubSubConnection
import kotlinx.coroutines.runBlocking

fun Application.configureRedis() {

    val redisClient = RedisClient.create("redis://localhost:6379")
    dependencies {
        provide<RedisClient> {
            redisClient
        }

        provide<StatefulRedisConnection<String, String>>("redis-string-string-connection") {
            redisClient.connect()
        }

        provide<StatefulRedisConnection<String, ByteArray>>("redis-string-bytes-connection") {
            redisClient.connect(StringByteArrayCodec())
        }

        provide<StatefulRedisPubSubConnection<String, String>>("redis-pub-sub-connection") {
            redisClient.connectPubSub()
        }


    }
    
    monitor.subscribe(ApplicationStopping) {
        runBlocking {
            dependencies.resolve<RedisSubscriber>().close()
            redisClient.shutdown()
        }
    }
}