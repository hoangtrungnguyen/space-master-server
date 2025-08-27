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
import io.lettuce.core.Range.Boundary.excluding
import io.lettuce.core.Range.Boundary.unbounded
import io.lettuce.core.StreamMessage
import io.lettuce.core.api.StatefulRedisConnection
import io.lettuce.core.pubsub.StatefulRedisPubSubConnection
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.*

class PullStreamContext(
    val redis: StatefulRedisConnection<String, ByteArray>,
    val redisPubSub: StatefulRedisPubSubConnection<String, String>
)

class PullStreamCommand(
    val process: Process,
    input: PullStreamInput,
) {
    val messageId = input.messageId
    val streamEntryId = input.streamEntryId
    val count = input.count

    fun execute(context: PullStreamContext) : Any {
        val streamKey = redisDocSyncEventsKey(process.docId)
        val range = Range.from(excluding(streamEntryId), unbounded())
        val limit = Limit.from(count)
        val streamMessages = context.redis.sync().xrange(streamKey, range, limit)

        val entries = streamMessages.filterNotNull().mapNotNull { message ->
            val body: Map<String, ByteArray?> = message.body
            val bytes: ByteArray? = body["bytes"]
            if (bytes != null) {
                val entry  = Json.decodeFromStream<DocumentSyncEventValue>(bytes.inputStream())
                entry.seid = message.id
                return@mapNotNull entry
            } else null
        }

        return StreamEntriesOutput(
            replyTo = messageId,
            entries = entries
        )
    }

}

@Serializable
data class StreamEntriesOutput(
    override val messageType: MessageType = MessageType.STREAM_ENTRIES,
    override val replyTo: String,
    val entries: List<DocumentSyncEventValue>
) : DocumentChannelOutput()
