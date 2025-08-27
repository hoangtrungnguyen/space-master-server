@file:OptIn(ExperimentalUuidApi::class, ExperimentalUuidApi::class, ExperimentalSerializationApi::class)

package com.ideaspace.core.kafkaMessage

import com.ideaspace.core.dto.NullableUUIDSerializer
import com.ideaspace.core.dto.UUIDToString
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import kotlinx.serialization.json.JsonClassDiscriminator
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import java.util.*
import kotlin.uuid.ExperimentalUuidApi

enum class SyncOperation {
    INIT_SYNC,
    EDIT_DOC,
    ADD_ELEMENT,
    EDIT_ELEMENT,
    MOVE_ELEMENT,
    REMOVE_ELEMENT,
    SAVE_DOC,
    FINISH_SYNC
}


sealed class DocumentEvent {}

@Serializable
@JsonClassDiscriminator("sync_op")
sealed class DocumentSyncEventValue : DocumentEvent() {
    abstract val syncOp: SyncOperation
    abstract val docId: Long
    abstract val processId: Long
    abstract val userId: Long
    abstract val windowId: Long
}

class UnknownDocEvent() : DocumentEvent()


@Serializable
@SerialName("INIT_SYNC")
data class InitSyncEventValue(
    @Transient override val syncOp: SyncOperation = SyncOperation.INIT_SYNC,
    override val docId: Long,
    override val processId: Long,
    override val userId: Long,
    override val windowId: Long,
    @Serializable(with = NullableUUIDSerializer::class)
    val peerUuid: UUID?,
) : DocumentSyncEventValue()

@Serializable
@SerialName("EDIT_DOC")
data class EditDocEventValue(
    @Transient override val syncOp: SyncOperation = SyncOperation.EDIT_DOC,
    override val docId: Long,
    override val processId: Long,
    override val userId: Long,
    override val windowId: Long,
    val payload: EditDocPayload
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

sealed class DocumentSyncPayload

@Serializable
open class InitSyncPayload(
    @Serializable(with = NullableUUIDSerializer::class)
    val peerUuid: UUID? = null,
) : DocumentSyncPayload()

@Serializable
@JsonClassDiscriminator("element_op")
sealed class EditDocPayload : DocumentSyncPayload() {
    abstract val elementOp: ElementOp
    abstract val element: Element
}

@Serializable
enum class ElementOp {
    ADD_ELEMENT,
    EDIT_ELEMENT,
    MOVE_ELEMENT,
    REMOVE_ELEMENT
}

// region Element Payload


@Serializable
@SerialName("ADD_ELEMENT")
data class AddElementPayload(
    @Transient override val elementOp: ElementOp = ElementOp.ADD_ELEMENT,
    @SerialName("element") override val element: AddElement
) : EditDocPayload()


@Serializable
@SerialName("EDIT_ELEMENT")
data class EditElementPayload(
    @Transient override val elementOp: ElementOp = ElementOp.EDIT_ELEMENT,
    @SerialName("element") override val element: EditElement
) : EditDocPayload()

@Serializable
@SerialName("MOVE_ELEMENT")
data class MoveElementPayload(
    @Transient override val elementOp: ElementOp = ElementOp.MOVE_ELEMENT,
    @SerialName("element") override val element: MoveElement
) : EditDocPayload()

@Serializable
@SerialName("REMOVE_ELEMENT")
data class RemoveElementPayload(
    @Transient override val elementOp: ElementOp = ElementOp.REMOVE_ELEMENT,
    @SerialName("element") override val element: RemoveElement
) : EditDocPayload()

// endregion

// region Element Body

@Serializable
sealed class Element {
    abstract val uuid: UUID
}

@Serializable
data class AddElement (
    @Serializable(UUIDToString::class)
    override val uuid: UUID,
    @SerialName("parent_uuid") @Serializable(UUIDToString::class)
    val parentUuid: UUID? = null,
    val metadata: JsonElement? = null,
    val type: String,
    val value: JsonElement
) : Element()

@Serializable
data class EditElement (
    @Serializable(UUIDToString::class)
    override val uuid: UUID,
    val metadata: JsonElement? = null,
    val type: String,
    val value: JsonElement
) : Element()

@Serializable
data class MoveElement (
    @Serializable(UUIDToString::class)
    override val uuid: UUID,
    @SerialName("parent_uuid") @Serializable(UUIDToString::class)
    val parentUuid: UUID? = null,
) : Element()

@Serializable
data class RemoveElement (
    @Serializable(UUIDToString::class)
    override val uuid: UUID,
) : Element()

// endregion