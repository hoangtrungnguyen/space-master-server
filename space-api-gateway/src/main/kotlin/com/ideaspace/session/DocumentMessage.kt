@file:OptIn(ExperimentalSerializationApi::class)

package com.ideaspace.session

import com.ideaspace.core.dto.NullableUUIDSerializer
import com.ideaspace.core.dto.UUIDToString
import com.ideaspace.core.kafkaMessage.*
import com.ideaspace.core.models.Process
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonClassDiscriminator
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.polymorphic
import java.util.*

enum class MessageType {
    // Document Stream Api

    INIT_SYNC,
    ADD_ELEMENT,
    EDIT_ELEMENT,
    MOVE_ELEMENT,
    REMOVE_ELEMENT,
    SAVE_DOC,
    FINISH_SYNC,

    STREAM_ADD_ENTRY,
    PULL_STREAM,
    STREAM_ENTRIES,

    ACK,

    LIST_PEER
}

@Serializable
@JsonClassDiscriminator("messageType")
sealed class DocumentChannelInput {
    abstract val messageId: String
    abstract val messageType: MessageType
}

@Serializable
abstract class DocumentChannelOutput {
    abstract val replyTo: String
    abstract val messageType: MessageType
}

// region Document Stream Api

@Serializable
sealed class DocumentFlowUpChange() : DocumentChannelInput() {
    abstract fun toDocumentSyncEventValue(process: Process): DocumentSyncEventValue
}

@Serializable
@SerialName("INIT_SYNC")
class InitSyncInput(
    override val messageId: String,
    @Transient
    override val messageType: MessageType = MessageType.INIT_SYNC,
    @Serializable(with = NullableUUIDSerializer::class)
    val peerUuid: UUID?,
) : DocumentFlowUpChange() {
    override fun toDocumentSyncEventValue(process: Process): DocumentSyncEventValue {
        return InitSyncEventValue(
            syncOp = SyncOperation.INIT_SYNC,
            docId = process.docId,
            processId = process.id,
            userId = process.userId,
            windowId = process.windowId,
            peerUuid = peerUuid
        )
    }
}

@Serializable
@SerialName("ADD_ELEMENT")
data class AddElementInput (
    override val messageId: String,
    @Transient
    override val messageType: MessageType = MessageType.ADD_ELEMENT,
    @Serializable(UUIDToString::class)
    val uuid: UUID,
    @Serializable(UUIDToString::class)
    val parentUuid: UUID? = null,
    val metadata: JsonObject? = null,
    val type: String,
    val value: JsonObject
) : DocumentFlowUpChange() {
    override fun toDocumentSyncEventValue(process: Process): DocumentSyncEventValue {
        return AddElementEventValue(
            syncOp = SyncOperation.ADD_ELEMENT,
            docId = process.docId,
            processId = process.id,
            userId = process.userId,
            windowId = process.windowId,
            uuid = uuid,
            parentUuid = parentUuid,
            metadata = metadata,
            type = type,
            value = value
        )
    }
}

@Serializable
@SerialName("EDIT_ELEMENT")
data class EditElementInput (
    override val messageId: String,
    @Transient
    override val messageType: MessageType = MessageType.EDIT_ELEMENT,
    @Serializable(UUIDToString::class)
    val uuid: UUID,
    val metadata: JsonObject? = null,
    val type: String,
    val value: JsonObject
) : DocumentFlowUpChange() {
    override fun toDocumentSyncEventValue(process: Process): DocumentSyncEventValue {
        return EditElementEventValue(
            syncOp = SyncOperation.EDIT_ELEMENT,
            docId = process.docId,
            processId = process.id,
            userId = process.userId,
            windowId = process.windowId,
            uuid = uuid,
            metadata = metadata,
            type = type,
            value = value
        )
    }
}


@Serializable
@SerialName("MOVE_ELEMENT")
data class MoveElementInput (
    override val messageId: String,
    @Transient
    override val messageType: MessageType = MessageType.MOVE_ELEMENT,
    @Serializable(UUIDToString::class)
    val uuid: UUID,
    @Serializable(UUIDToString::class)
    val parentUuid: UUID? = null,
) : DocumentFlowUpChange() {
    override fun toDocumentSyncEventValue(process: Process): DocumentSyncEventValue {
        return MoveElementEventValue(
            syncOp = SyncOperation.MOVE_ELEMENT,
            docId = process.docId,
            processId = process.id,
            userId = process.userId,
            windowId = process.windowId,
            uuid = uuid,
            parentUuid = parentUuid,
        )
    }
}

@Serializable
@SerialName("REMOVE_ELEMENT")
data class RemoveElementInput (
    override val messageId: String,
    @Transient
    override val messageType: MessageType = MessageType.REMOVE_ELEMENT,
    @Serializable(UUIDToString::class)
    val uuid: UUID,
) : DocumentFlowUpChange() {
    override fun toDocumentSyncEventValue(process: Process): DocumentSyncEventValue {
        return RemoveElementEventValue(
            syncOp = SyncOperation.REMOVE_ELEMENT,
            docId = process.docId,
            processId = process.id,
            userId = process.userId,
            windowId = process.windowId,
            uuid = uuid,
        )
    }
}


@Serializable
@SerialName("SAVE_DOC")
class SaveDocInput(
    override val messageId: String,
    @Transient
    override val messageType: MessageType = MessageType.SAVE_DOC
) : DocumentFlowUpChange() {
    override fun toDocumentSyncEventValue(process: Process): DocumentSyncEventValue {
        return SaveDocEventValue(
            syncOp = SyncOperation.SAVE_DOC,
            docId = process.docId,
            processId = process.id,
            userId = process.userId,
            windowId = process.windowId,
        )
    }
}


@Serializable
@SerialName("FINISH_SYNC")
class FinishSyncInput(
    override val messageId: String,
    @Transient
    override val messageType: MessageType = MessageType.FINISH_SYNC
) : DocumentFlowUpChange() {
    override fun toDocumentSyncEventValue(process: Process): DocumentSyncEventValue {
        return FinishSyncEventValue(
            syncOp = SyncOperation.FINISH_SYNC,
            docId = process.docId,
            processId = process.id,
            userId = process.userId,
            windowId = process.windowId,
        )
    }
}

@Serializable
data class StreamAddEntry(
    override val replyTo: String = "NONE",
    override val messageType: MessageType = MessageType.STREAM_ADD_ENTRY,
    val entryId: String
) : DocumentChannelOutput()

@Serializable
@SerialName("PULL_STREAM")
data class PullStreamInput(
    override val messageId: String,
    @Transient
    override val messageType: MessageType = MessageType.PULL_STREAM,
    val streamCursor: String,
    val count: Long
) : DocumentChannelInput()

// endregion

@Serializable
data class Acknowledgement(
    override val replyTo: String,
    override val messageType: MessageType = MessageType.ACK,
    val message: String
) : DocumentChannelOutput()

val ChannelJson = Json {
    encodeDefaults = true
    serializersModule = SerializersModule {
        polymorphic(DocumentChannelOutput::class) {
            subclass(StreamAddEntry::class, StreamAddEntry.serializer())
            subclass(Acknowledgement::class, Acknowledgement.serializer())
        }
    }
}


//region Peer2Peer
@Serializable
data class ListPeerOut(
    val peerCount: Int,
    @Serializable(with = UUIDToString::class)
    val removedPeer: UUID? = null,
    @Serializable(with = UUIDToString::class)
    val newPeer: UUID? = null,
    val listPeer: List<@Serializable(with = UUIDToString::class) UUID>,
) : DocumentChannelOutput() {
    override val messageType: MessageType
        get() = MessageType.LIST_PEER
    override val replyTo: String
        get() = newPeer?.toString() ?: removedPeer?.toString() ?: "NONE"
}


/**
 * Converts a Map<String, String> from a Redis Stream message
 * into a ListPeerOut object.
 *
 * @param data The body of the StreamMessage.
 * @return A ListPeerOut object.
 * @throws IllegalArgumentException if required fields are missing or malformed.
 */
fun Map<String, String>.toListPeerOut(): ListPeerOut {
    val data = this
    try {
        // 1. Parse Int
        val peerCount = data["peerCount"]?.toInt()
            ?: throw IllegalArgumentException("Missing or invalid 'peerCount'")

        // 2. Parse nullable UUIDs
        val removedPeer = data["removedPeer"]?.let { UUID.fromString(it) }
        val newPeer = data["newPeer"]?.let { UUID.fromString(it) }

        // 3. Parse the List<UUID>
        // This assumes the list is stored as a single comma-separated string.
        // e.g., "uuid1,uuid2,uuid3"
        val listPeer = data["listPeer"]
            ?.takeIf { it.isNotBlank() } // Handle empty string case
            ?.split(',')                 // Split by comma
            ?.map { it.trim() }          // Trim whitespace
            ?.map { UUID.fromString(it) } // Convert each part to a UUID
            ?: emptyList()               // Default to an empty list if the field is missing

        // 4. Construct and return the object
        return ListPeerOut(
            peerCount = peerCount,
            removedPeer = removedPeer,
            newPeer = newPeer,
            listPeer = listPeer
        )
    } catch (e: Exception) {
        // Catch parsing errors (e.g., bad UUID format, non-integer)
        println("Error converting Redis map to ListPeerOut: ${e.message}")
        throw IllegalArgumentException("Failed to parse stream message body.", e)
    }
}

//endregion
