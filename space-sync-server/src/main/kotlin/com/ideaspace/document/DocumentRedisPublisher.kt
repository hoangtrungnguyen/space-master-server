@file:OptIn(ExperimentalSerializationApi::class)

package com.ideaspace.document

import com.ideaspace.core.kafkaMessage.DocumentSyncEventValue
import com.ideaspace.core.kafkaMessage.FinishSyncEventValue
import com.ideaspace.core.kafkaMessage.SaveDocEventValue
import com.ideaspace.core.redis.RedisManager
import com.ideaspace.core.redis.redisDocKeyPattern
import com.ideaspace.core.redis.toBytes
import io.lettuce.core.api.sync.RedisCommands
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.encodeToStream
import java.io.ByteArrayOutputStream

class DocumentRedisPublisher(
    private val streamMaxLen: Long = 10_000L
) {
    suspend fun publishEditDocEvent(
        docId: Long,
        processId: Long,
        redisDocumentEvent: DocumentSyncEventValue
    ): String {

        val redisKey = redisDocKeyPattern(docId)

        val syncCommands: RedisCommands<String, ByteArray> = RedisManager.connection.sync()

        return syncCommands.startXAdd(redisDocumentEvent, redisKey, processId)
    }

    suspend fun publishFinishSyncDocEvent(
        redisDocumentEvent: FinishSyncEventValue
    ) {
        val redisKey = redisDocKeyPattern(redisDocumentEvent.docId)
        val syncCommands: RedisCommands<String, ByteArray> = RedisManager.connection.sync()
        syncCommands.startXAdd(redisDocumentEvent, redisKey, redisDocumentEvent.processId)
    }

    suspend fun publishSaveDocEvent(
        redisDocumentEvent: SaveDocEventValue
    ) {
        val redisKey = redisDocKeyPattern(redisDocumentEvent.docId)
        val syncCommands: RedisCommands<String, ByteArray> = RedisManager.connection.sync()
        syncCommands.startXAdd(redisDocumentEvent, redisKey, redisDocumentEvent.processId)
    }

    private fun RedisCommands<String, ByteArray>.startXAdd(
        redisDocumentEvent: DocumentSyncEventValue,
        redisKey: String,
        processId: Long
    ): String {
        val value = ByteArrayOutputStream().use { outputStream ->
            Json.encodeToStream(redisDocumentEvent, outputStream)
            outputStream.toByteArray()
        }

        val messageId = this.xadd(redisKey, mapOf(
            "sourceProcessId" to redisDocumentEvent.processId.toBytes(),
            "bytes" to value
        ))
        return messageId.also {
            println("✅ Saved operation ${processId} to Redis stream '$redisKey' with message ID $messageId")
        }
    }

    suspend fun publishDocProcess(
        docId: Long,
        processId: Long,
    ) {
        // TODO DO NOT do this in sync server
        // TODO List of active processes of a document should have a proper key like 'ideaspace:doc:$docId:processes:active'
        // TODO Should not be a stream, but a REDIS LIST. See redis list API for add/remove item from list.
    }
}