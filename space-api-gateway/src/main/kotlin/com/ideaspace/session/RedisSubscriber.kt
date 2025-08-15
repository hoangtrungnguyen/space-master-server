package com.ideaspace.session

import com.ideaspace.core.redis.redisKey
import io.ktor.server.application.Application
import io.ktor.server.application.log
import io.ktor.utils.io.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import redis.clients.jedis.JedisPool
import redis.clients.jedis.JedisPoolConfig
import redis.clients.jedis.StreamEntryID
import redis.clients.jedis.exceptions.JedisConnectionException
import redis.clients.jedis.params.XReadParams

class RedisSubscriber(
    val docId: Long,
    val processId: Long
) {

    fun subscribe(application: Application, handler: (message: String) -> Unit) {
        println("Subscribing to Redis channel for document: $docId")
        // Example: jedis.subscribe(MyJedisPubSub(handler), docId)
//        val listenerJob: Job =
        val host = application.environment.config.property("redis.host").getString()
        val port: Int = application.environment.config.property("redis.port").getString().toInt()
        val jedisPool = JedisPool(JedisPoolConfig(), host, port)
        val streamKey = redisKey(docId, processId)

        val streamReadCount = 100
        application.launch(Dispatchers.IO) {
            val streamId = StreamEntryID(streamKey)
            while (isActive) {

                try {
                    jedisPool.resource.use { jedis ->
                        val streams = mapOf(streamKey to streamId)
                        // Block for up to `streamBlockMillis` waiting for new messages
                        val response = jedis.xread(
                            XReadParams(
                            ).count(streamReadCount), streams
                        )

                        response?.forEach { (sName, entries) ->
                            if (entries.isNotEmpty()) {
                                application.log.info("Received ${entries.size} new messages from stream '$sName'")
                                entries.forEach { entry ->
                                    application.log.debug("Message ID: ${entry.id}, Fields: ${entry.fields}")
                                }
                            }
                        }
                    }
                } catch (e: JedisConnectionException) {
                    application.log.error("Redis connection error in stream listener. Retrying in 5s.", e)
                    delay(5000) // Wait before retrying connection
                } catch (e: CancellationException) {
                    application.log.info("Redis stream listener is stopping.")
                    break // Exit loop on cancellation
                } catch (e: Exception) {
                    application.log.error("Error in Redis stream listener. Retrying in 1s.", e)
                    delay(1000) // Wait a bit before retrying on other errors
                }
            }
        }
    }

    fun unsubscribe(docId: String) {
        println("Unsubscribing from Redis channel for document: $docId")
    }

}