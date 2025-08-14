package com.ideaspace.workers

import com.ideaspace.core.redis.RedisSyncOperation
import redis.clients.jedis.JedisPool
import kotlinx.serialization.json.Json
import org.slf4j.LoggerFactory
import redis.clients.jedis.params.XAddParams


class RedisPublisher (
    private val jedisPool: JedisPool,
    private val streamMaxLen: Long = 10_000L
){
    private val logger = LoggerFactory.getLogger(RedisPublisher::class.java)

    fun saveSyncOperation( operation: RedisSyncOperation) {
        logger.info(operation.toString())

        val redisKey = "op:${operation.processId}"
        val payloadJson = Json.encodeToString(operation.payload)

        val operationMap = mapOf(
            "sync_op" to operation.syncOp,
            "doc_id" to operation.docId.toString(),
            "process_id" to operation.processId.toString(),
            "user_id" to operation.userId.toString(),
            "session_id" to operation.sessionId.toString(),
            "client_id" to operation.clientId.toString(),
            "payload" to payloadJson // Store the nested object as a string
        )


        val params = XAddParams.xAddParams().maxLen(streamMaxLen)

        jedisPool.resource.use { jedis ->
            jedis.xadd(redisKey,params,  operationMap)
        }

        println("✅ Saved operation ${operation.processId} to Redis hash '$redisKey'")
    }
}