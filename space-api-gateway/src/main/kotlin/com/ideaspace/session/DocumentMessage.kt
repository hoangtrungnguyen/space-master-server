@file:OptIn(ExperimentalSerializationApi::class)

package com.ideaspace.session

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
import kotlinx.serialization.json.JsonClassDiscriminator
import kotlinx.serialization.json.JsonObject
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
}

@Serializable
@JsonClassDiscriminator("messageType")
sealed class DocumentChannelInput {
    abstract val messageId: String
    abstract val messageType: MessageType
}

@Serializable
@JsonClassDiscriminator("messageType")
sealed class DocumentChannelOutput {
    abstract val replyTo: String
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
            syncOp = SyncOperation.INIT_SYNC,
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
@SerialName("STREAM_ADD_ENTRY")
data class StreamAddEntry(
    override val replyTo: String = "NONE",
    @SerialName("seid")
    val streamEntryId: String
) : DocumentChannelOutput()

@Serializable
@SerialName("PULL_STREAM")
data class PullStreamInput(
    override val messageId: String,
    @Transient
    override val messageType: MessageType = MessageType.PULL_STREAM,
    @SerialName("seid")
    val streamEntryId: String,
    val count: Int
) : DocumentChannelInput()

@Serializable
@SerialName("STREAM_ENTRIES")
data class StreamEntriesOutput(
    override val replyTo: String,
    val entries: List<DocumentSyncEventValue>
) : DocumentChannelOutput()

// endregion

@Serializable
@SerialName("ACK")
data class Acknowledgement(
    override val replyTo: String,
    val message: String
) : DocumentChannelOutput()