@file:OptIn(ExperimentalUuidApi::class, ExperimentalUuidApi::class, ExperimentalSerializationApi::class)

package com.ideaspace.core.kafkaMessage

import com.ideaspace.core.dto.UUIDToString
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
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

@Serializable
@JsonClassDiscriminator("sync_op")
sealed class DocumentSyncEventValue {
    //// NOTE: This field is only used when returning to process as stream entries
    var entryId: String? = null
    //// END OF NOTE
    abstract val syncOp: SyncOperation
    abstract val docId: Long
    abstract val processId: Long
    abstract val userId: Long
    abstract val windowId: Long
}

@Serializable
@SerialName("INIT_SYNC")
data class InitSyncEventValue(
    @Transient override val syncOp: SyncOperation = SyncOperation.INIT_SYNC,
    override val docId: Long,
    override val processId: Long,
    override val userId: Long,
    override val windowId: Long
) : DocumentSyncEventValue()

@Serializable
@SerialName("ADD_ELEMENT")
data class AddElementEventValue(
    @Transient override val syncOp: SyncOperation = SyncOperation.ADD_ELEMENT,
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
    @Transient override val syncOp: SyncOperation = SyncOperation.EDIT_ELEMENT,
    override val docId: Long,
    override val processId: Long,
    override val userId: Long,
    override val windowId: Long,

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
    @Transient override val syncOp: SyncOperation = SyncOperation.MOVE_ELEMENT,
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
    @Transient override val syncOp: SyncOperation = SyncOperation.REMOVE_ELEMENT,
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
    @Transient override val syncOp: SyncOperation = SyncOperation.SAVE_DOC,
    override val docId: Long,
    override val processId: Long,
    override val userId: Long,
    override val windowId: Long,
) : DocumentSyncEventValue()


@Serializable
@SerialName("FINISH_SYNC")
data class FinishSyncEventValue(
    @Transient override val syncOp: SyncOperation = SyncOperation.FINISH_SYNC,
    override val docId: Long,
    override val processId: Long,
    override val userId: Long,
    override val windowId: Long,
) : DocumentSyncEventValue()
