@file:OptIn(ExperimentalSerializationApi::class)

package com.ideaspace.session

import com.ideaspace.config.ErrorResponse
import com.ideaspace.core.dto.UUIDToString
import com.ideaspace.core.kafkaMessage.AddElementEventValue
import com.ideaspace.core.kafkaMessage.DocumentSyncEventValue
import com.ideaspace.core.kafkaMessage.EditElementEventValue
import com.ideaspace.core.kafkaMessage.FinishSyncEventValue
import com.ideaspace.core.kafkaMessage.InitSyncEventValue
import com.ideaspace.core.kafkaMessage.MoveElementEventValue
import com.ideaspace.core.kafkaMessage.RemoveElementEventValue
import com.ideaspace.core.kafkaMessage.SaveDocEventValue
import com.ideaspace.core.kafkaMessage.SyncOperation
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
    // InputMessageType

    INIT_SYNC,
    ADD_ELEMENT,
    EDIT_ELEMENT,
    MOVE_ELEMENT,
    REMOVE_ELEMENT,
    SAVE_DOC,
    FINISH_SYNC,

    // OutputMessageType
    STREAM_ADD_ENTRY,
    PULL_STREAM,
    STREAM_ENTRIES,

    CONNECTED,
    ERROR,
    ACK,
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
    override val messageType: MessageType = MessageType.INIT_SYNC
) : DocumentFlowUpChange() {
    override fun toDocumentSyncEventValue(process: Process): DocumentSyncEventValue {
        return InitSyncEventValue(
            docId = process.docId,
            processId = process.id,
            userId = process.userId,
            windowId = process.windowId
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
    @Serializable(UUIDToString::class)
    val parentUuid: UUID? = null,
    val metadata: JsonObject? = null,
    val type: String,
    val value: JsonObject
) : DocumentFlowUpChange() {
    override fun toDocumentSyncEventValue(process: Process): DocumentSyncEventValue {
        return EditElementEventValue(
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
    val entryId: String,
    @Transient
    val sourceProcessId: Long = -1,
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
data class ConnectedOutput(
    override val replyTo: String,
    override val messageType: MessageType = MessageType.CONNECTED,
    val message: String,
    val loginName: String,
) : DocumentChannelOutput()


@Serializable
data class ErrorOutput(
    override val replyTo: String,
    override val messageType: MessageType = MessageType.ERROR,
    val error: ErrorResponse
) : DocumentChannelOutput()

@Serializable
data class Acknowledgement(
    override val replyTo: String,
    override val messageType: MessageType = MessageType.ACK,
    val message: String
) : DocumentChannelOutput()

val ChannelJson = Json {
    encodeDefaults = true
    ignoreUnknownKeys = true
    serializersModule = SerializersModule {
        polymorphic(DocumentChannelOutput::class) {
            subclass(StreamAddEntry::class, StreamAddEntry.serializer())
        }
    }
}