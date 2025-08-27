@file:OptIn(ExperimentalSerializationApi::class)

package com.ideaspace.document

import com.ideaspace.core.kafkaMessage.DocumentSyncEventValue
import com.ideaspace.core.models.Process
import com.ideaspace.core.redis.redisDocSyncEventsKey
import com.ideaspace.session.DocumentChannelOutput
import com.ideaspace.session.MessageType
import com.ideaspace.session.PullStreamInput
import io.lettuce.core.Limit
import io.lettuce.core.Range
import io.lettuce.core.Range.Boundary.including
import io.lettuce.core.Range.Boundary.unbounded
import io.lettuce.core.api.StatefulRedisConnection
import io.lettuce.core.pubsub.StatefulRedisPubSubConnection
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.decodeFromStream

class PullStreamContext(
    val redis: StatefulRedisConnection<String, ByteArray>,
    val redisPubSub: StatefulRedisPubSubConnection<String, String>
)

class PullStreamCommand(
    val process: Process,
    input: PullStreamInput,
) {
    val messageId = input.messageId
    val streamCursor = input.streamCursor
    val count = input.count

    fun execute(context: PullStreamContext) : Any {
        val streamKey = redisDocSyncEventsKey(process.docId)
        val range = Range.from(including(streamCursor), unbounded())
        val limit = Limit.from(count)
        val streamMessages = context.redis.sync().xrange(streamKey, range, limit)

        // Exclude last stream entry by prefixing REDIS STREAM range operator "("
        val lastEntryId = streamMessages.lastOrNull()?.id
        val nextCursor = if (lastEntryId != null) "($lastEntryId" else streamCursor
        val entries = streamMessages.filterNotNull().mapNotNull { message ->
            val body: Map<String, ByteArray?> = message.body
            val bytes: ByteArray? = body["bytes"]
            if (bytes != null) {
                val entry  = Json.decodeFromStream<DocumentSyncEventValue>(bytes.inputStream())
                entry.entryId = message.id
                return@mapNotNull entry
            } else null
        }

        return StreamEntriesOutput(
            replyTo = messageId,
            nextCursor = nextCursor,
            // We need to count actual returned messages.
            endOfStream = streamMessages.isEmpty(),
            entries = entries
        )
    }

}

@Serializable
data class StreamEntriesOutput(
    override val replyTo: String,
    override val messageType: MessageType = MessageType.STREAM_ENTRIES,
    val nextCursor: String,
    val endOfStream: Boolean,
    val entries: List<DocumentSyncEventValue>
) : DocumentChannelOutput()
