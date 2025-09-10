@file:OptIn(ExperimentalUuidApi::class, ExperimentalUuidApi::class, ExperimentalSerializationApi::class)

package com.ideaspace.core.kafkaMessage

import com.ideaspace.core.dto.NullableUUIDSerializer
import com.ideaspace.core.dto.UUIDToString
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonClassDiscriminator
import kotlinx.serialization.json.JsonObject
import java.util.*
import kotlin.uuid.ExperimentalUuidApi

enum class SyncOperation {
    INIT_SYNC,
    ADD_ELEMENT,
    EDIT_ELEMENT,
    MOVE_ELEMENT,
    REMOVE_ELEMENT,
    SAVE_DOC,
    FINISH_SYNC
}


sealed class DocumentEvent {}

@Serializable
@JsonClassDiscriminator("syncOp")
sealed class DocumentSyncEventValue {
    //// NOTE: This field is only used when returning to process as stream entries
    var entryId: String? = null
    //// END OF NOTE
    abstract val docId: Long

    /** Process that initiates this event */
    abstract val processId: Long
    /** User that initiates this event */
    abstract val userId: Long
    /** Window that initiates this event */
    abstract val windowId: Long
}

class UnknownDocEvent() : DocumentEvent()


@Serializable
@SerialName("INIT_SYNC")
data class InitSyncEventValue(
    override val docId: Long,
    override val processId: Long,
    override val userId: Long,
    override val windowId: Long,
    @Serializable(with = NullableUUIDSerializer::class)
    val peerUuid: UUID?,
) : DocumentSyncEventValue()

@Serializable
@SerialName("ADD_ELEMENT")
data class AddElementEventValue(
    override val docId: Long,
    override val processId: Long,
    override val userId: Long,
    override val windowId: Long,

    // Specific properties
    @Serializable(UUIDToString::class)
    val uuid: UUID,
    @Serializable(UUIDToString::class)
    val parentUuid: UUID? = null,
    val metadata: JsonObject? = null,
    val type: String,
    val value: JsonObject
) : DocumentSyncEventValue()

@Serializable
@SerialName("EDIT_ELEMENT")
data class EditElementEventValue(
    override val docId: Long,
    override val processId: Long,
    override val userId: Long,
    override val windowId: Long,
    val revision: Long = -1,
    // Specific properties
    @Serializable(UUIDToString::class)
    val uuid: UUID,
    val metadata: JsonObject? = null,
    val type: String,
    val value: JsonObject
) : DocumentSyncEventValue()

@Serializable
@SerialName("MOVE_ELEMENT")
data class MoveElementEventValue (
    override val docId: Long,
    override val processId: Long,
    override val userId: Long,
    override val windowId: Long,

    // Specific properties
    @Serializable(UUIDToString::class)
    val uuid: UUID,
    @Serializable(UUIDToString::class)
    val parentUuid: UUID? = null,
) : DocumentSyncEventValue()


@Serializable
@SerialName("REMOVE_ELEMENT")
data class RemoveElementEventValue (
    override val docId: Long,
    override val processId: Long,
    override val userId: Long,
    override val windowId: Long,

    @Serializable(UUIDToString::class)
    val uuid: UUID,
) : DocumentSyncEventValue()

@Serializable
@SerialName("SAVE_DOC")
data class SaveDocEventValue(
    override val docId: Long,
    override val processId: Long,
    override val userId: Long,
    override val windowId: Long,
) : DocumentSyncEventValue()


@Serializable
@SerialName("FINISH_SYNC")
data class FinishSyncEventValue(
    override val docId: Long,
    override val processId: Long,
    override val userId: Long,
    override val windowId: Long,
) : DocumentSyncEventValue()
