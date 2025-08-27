package com.ideaspace.document

import com.ideaspace.core.kafkaMessage.DocumentSyncEventValue
import com.ideaspace.core.kafkaMessage.FinishSyncEventValue
import com.ideaspace.core.kafkaMessage.SaveDocEventValue
import com.ideaspace.core.redis.RedisManager
import com.ideaspace.core.redis.redisDocKey
import com.ideaspace.core.redis.redisDocSyncEventsKey
import io.lettuce.core.api.sync.RedisCommands
import kotlinx.serialization.json.*

class DocumentRedisPublisher(
    private val streamMaxLen: Long = 10_000L
) {
    suspend fun publishEditDocEvent(
        docId: Long,
        processId: Long,
        redisDocumentEvent: DocumentSyncEventValue
    ): String {

        val redisKey = redisDocSyncEventsKey(docId)

        val syncCommands: RedisCommands<String, String> = RedisManager.connection.sync()

        return syncCommands.startXAdd(redisDocumentEvent, redisKey, processId)
    }

    suspend fun publishFinishSyncDocEvent(
        redisDocumentEvent: FinishSyncEventValue
    ) {
        val redisKey = redisDocSyncEventsKey(redisDocumentEvent.docId)
        val syncCommands: RedisCommands<String, String> = RedisManager.connection.sync()
        syncCommands.startXAdd(redisDocumentEvent, redisKey, redisDocumentEvent.processId)
    }

    suspend fun publishSaveDocEvent(
        redisDocumentEvent: SaveDocEventValue
    ) {
        val redisKey = redisDocSyncEventsKey(redisDocumentEvent.docId )
        val syncCommands: RedisCommands<String, String> = RedisManager.connection.sync()
        syncCommands.startXAdd(redisDocumentEvent, redisKey, redisDocumentEvent.processId)
    }

    private fun RedisCommands<String, String>.startXAdd(
        redisDocumentEvent: DocumentSyncEventValue,
        redisKey: String,
        processId: Long
    ): String {
        val jsonElement = Json.encodeToJsonElement(redisDocumentEvent)
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
                println("✅ Saved operation ${processId} to Redis stream '$redisKey' with message ID $messageId")
            }
        } else {
            throw Exception("The provided event did not serialize to a JSON object, cannot publish to Redis stream.")

        }
    }

    // TODO DO NOT do this in sync server
    suspend fun publishDocProcess(
        docId: Long,
        processId: Long,
    ) {
        // TODO List of active processes of a document should have a proper key like 'ideaspace:doc:$docId:processes:active'
        val redisKey = redisDocKey(docId)

        val syncCommands: RedisCommands<String, String> = RedisManager.connection.sync()
        // 4. Use the XADD command to publish the message
        // The "*" tells Redis to generate a unique ID for this entry automatically.
        // TODO Should not be a stream, but a REDIS LIST. See redis list API for add/remove item from list.
        val messageId = syncCommands.xadd(
            redisKey,
            mapOf("processId" to processId.toString())
        )

        println("✅ Saved processId ${processId} to Redis stream '$redisKey' with message ID $messageId")
    }
}