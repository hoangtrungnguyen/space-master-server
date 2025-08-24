@file:OptIn(ExperimentalUuidApi::class, ExperimentalUuidApi::class, ExperimentalSerializationApi::class)

package com.ideaspace.core.kafkaMessage

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
    abstract val sessionId: Long
    abstract val clientId: Long
    abstract val payload: DocumentSyncPayload
}

class UnknownDocEvent() : DocumentEvent()

@Serializable
@SerialName("INIT_SYNC")
data class InitSyncEventValue(
    @Transient override val syncOp: SyncOperation = SyncOperation.INIT_SYNC,
    @SerialName("doc_id") override val docId: Long,
    @SerialName("process_id") override val processId: Long,
    @SerialName("user_id") override val userId: Long,
    @SerialName("session_id") override val sessionId: Long,
    @SerialName("client_id") override val clientId: Long,
    @SerialName("payload") override val payload: InitSyncPayload
) : DocumentSyncEventValue()

@Serializable
@SerialName("EDIT_DOC")
data class EditDocEventValue(
    @Transient override val syncOp: SyncOperation = SyncOperation.EDIT_DOC,
    @SerialName("doc_id") override val docId: Long,
    @SerialName("process_id") override val processId: Long,
    @SerialName("user_id") override val userId: Long,
    @SerialName("session_id") override val sessionId: Long,
    @SerialName("client_id") override val clientId: Long,
    @SerialName("payload") override val payload: EditDocPayload
) : DocumentSyncEventValue()


@Serializable
@SerialName("SAVE_DOC")
data class SaveDocEventValue(
    @Transient override val syncOp: SyncOperation = SyncOperation.SAVE_DOC,
    @SerialName("doc_id") override val docId: Long,
    @SerialName("process_id") override val processId: Long,
    @SerialName("user_id") override val userId: Long,
    @SerialName("session_id") override val sessionId: Long,
    @SerialName("client_id") override val clientId: Long,
    @SerialName("payload") override val payload: SaveDocPayload
) : DocumentSyncEventValue()



@Serializable
@SerialName("FINISH_SYNC")
data class FinishSyncEventValue(
    @Transient override val syncOp: SyncOperation = SyncOperation.FINISH_SYNC,
    @SerialName("doc_id") override val docId: Long,
    @SerialName("process_id") override val processId: Long,
    @SerialName("user_id") override val userId: Long,
    @SerialName("session_id") override val sessionId: Long,
    @SerialName("client_id") override val clientId: Long,
    @SerialName("payload") override val payload: FinishSyncPayload
) : DocumentSyncEventValue()

sealed class DocumentSyncPayload

@Serializable
open class InitSyncPayload() : DocumentSyncPayload()

@Serializable
@JsonClassDiscriminator("element_op")
sealed class EditDocPayload : DocumentSyncPayload() {
    abstract val elementOp: ElementOp
    abstract val element: Element
}

@Serializable
open class SaveDocPayload() : DocumentSyncPayload() {

}

@Serializable
open class FinishSyncPayload() : DocumentSyncPayload() {

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
    @Transient             override val elementOp: ElementOp = ElementOp.ADD_ELEMENT,
    @SerialName("element") override val element: AddElement
) : EditDocPayload()


@Serializable
@SerialName("EDIT_ELEMENT")
data class EditElementPayload(
    @Transient             override val elementOp: ElementOp = ElementOp.EDIT_ELEMENT,
    @SerialName("element") override val element: EditElement
) : EditDocPayload()

@Serializable
@SerialName("MOVE_ELEMENT")
data class MoveElementPayload(
    @Transient             override val elementOp: ElementOp = ElementOp.MOVE_ELEMENT,
    @SerialName("element") override val element: MoveElement
) : EditDocPayload()

@Serializable
@SerialName("REMOVE_ELEMENT")
data class RemoveElementPayload(
    @Transient             override val elementOp: ElementOp = ElementOp.REMOVE_ELEMENT,
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
    @Serializable(with = UUIDToString::class)
    override val uuid: UUID,
    @SerialName("parent_uuid") @Serializable(with = UUIDToString::class)
    val parentUuid: UUID? = null,
    val metadata: JsonElement = JsonObject(emptyMap()),
    val type: String,
    val value: JsonElement
) : Element()

@Serializable
data class EditElement (
    @Serializable(with = UUIDToString::class)
    override val uuid: UUID,
    val metadata: JsonElement = JsonObject(emptyMap()),
    val type: String,
    val value: JsonElement
) : Element()

@Serializable
data class MoveElement (
    @Serializable(with = UUIDToString::class)
    override val uuid: UUID,
    @SerialName("parent_uuid") @Serializable(with = UUIDToString::class)
    val parentUuid: UUID? = null,
) : Element()

@Serializable
data class RemoveElement (
    @Serializable(with = UUIDToString::class)
    override val uuid: UUID,
) : Element()

// endregion