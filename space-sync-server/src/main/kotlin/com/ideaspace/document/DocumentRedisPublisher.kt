package com.ideaspace.document

import com.ideaspace.core.redis.RedisDocumentEvent
import com.ideaspace.core.redis.redisDocKey
import com.ideaspace.core.redis.redisDocProcessKey
import com.ideaspace.core.redis.RedisManager
import io.lettuce.core.api.sync.RedisCommands
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.encodeToJsonElement
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.jsonObject

class DocumentRedisPublisher(
    private val streamMaxLen: Long = 10_000L
){
    suspend fun publishEditDocEvent(
        docId: Long,
        processId: Long,
        redisDocumentEvent: RedisDocumentEvent){

        val redisKey = redisDocProcessKey(docId, processId)

        val syncCommands: RedisCommands<String, String> = RedisManager.connection.sync()

        val jsonElement = Json.encodeToJsonElement(redisDocumentEvent)
        if (jsonElement is JsonObject) {
            val redisMap: Map<String, String> = jsonElement.jsonObject.mapValues { (_, value) ->
                if (value is JsonPrimitive) {
                    value.content
                } else {
                    Json.encodeToString(JsonElement.serializer(), value)
                }
            }
            val messageId = syncCommands.xadd(redisKey, redisMap)
            println("✅ Saved operation ${processId} to Redis stream '$redisKey' with message ID $messageId")
        } else {
            println("The provided event did not serialize to a JSON object, cannot publish to Redis stream.")
        }
    }

    suspend fun publishDocProcess(
        docId: Long,
        processId: Long,
    ){
        val redisKey = redisDocKey(docId)

        val syncCommands: RedisCommands<String, String> = RedisManager.connection.sync()
        // 4. Use the XADD command to publish the message
        // The "*" tells Redis to generate a unique ID for this entry automatically.
        val messageId = syncCommands.xadd(
            redisKey,
            mapOf("processId" to processId.toString())
        )

        println("✅ Saved processId ${processId} to Redis stream '$redisKey' with message ID $messageId")
    }
}