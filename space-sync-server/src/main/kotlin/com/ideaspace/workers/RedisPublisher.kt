package com.ideaspace.workers

import com.ideaspace.core.redis.RedisDocumentEvent
import com.ideaspace.core.redis.redisKey
import redis.clients.jedis.JedisPool
import kotlinx.serialization.json.Json
import org.slf4j.LoggerFactory
import redis.clients.jedis.params.XAddParams


class RedisPublisher (
    private val jedisPool: JedisPool,
    private val streamMaxLen: Long = 10_000L
){
    private val logger = LoggerFactory.getLogger(RedisPublisher::class.java)

    fun publishDocumentEvent(documentEvent: RedisDocumentEvent) {
        logger.info(documentEvent.toString())

        val redisKey = redisKey(documentEvent.docId, documentEvent.processId)
        val payloadJson = Json.encodeToString(documentEvent.payload)

        val operationMap = mapOf(
            "sync_op" to documentEvent.syncOp,
            "doc_id" to documentEvent.docId.toString(),
            "process_id" to documentEvent.processId.toString(),
            "user_id" to documentEvent.userId.toString(),
            "session_id" to documentEvent.sessionId.toString(),
            "client_id" to documentEvent.clientId.toString(),
            "payload" to payloadJson // Store the nested object as a string
        )


        val params = XAddParams.xAddParams().maxLen(streamMaxLen)

        jedisPool.resource.use { jedis ->
            jedis.xadd(redisKey,params,  operationMap)
        }

        println("✅ Saved operation ${documentEvent.processId} to Redis hash '$redisKey'")
    }
}