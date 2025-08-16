@file:OptIn(ExperimentalUuidApi::class, ExperimentalUuidApi::class, ExperimentalSerializationApi::class)

package com.ideaspace.core.kafkaMessage

import com.ideaspace.core.dto.UUIDToString
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonClassDiscriminator
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import java.util.*
import kotlin.uuid.ExperimentalUuidApi

@Serializable
data class DocumentSyncEventValue(

    @SerialName("sync_op")
    val syncOp: SyncOperation,

    @SerialName("doc_id")
    val docId: Long,

    @SerialName("process_id")
    val processId: Long,

    @SerialName("user_id")
    val userId: Long,

    @SerialName("session_id")
    val sessionId: Long,

    @SerialName("client_id")
    val clientId: Long,

    @SerialName("payload")
    val payload: ElementPayload
)


enum class SyncOperation {
    INIT_SYNC,
    EDIT_DOC,
    SAVE_DOC,
    FINISH_SYNC
}

@Serializable
enum class ElementOp {
    ADD_ELEMENT,
    EDIT_ELEMENT,
    MOVE_ELEMENT,
    REMOVE_ELEMENT
}

// region Element Payloads

@Serializable
@JsonClassDiscriminator("element_op")
sealed class ElementPayload {
    abstract fun elementOp(): ElementOp
    abstract val element: Element
}

@Serializable
@SerialName("ADD_ELEMENT")
data class AddElementPayload(
    @SerialName("element")
    override val element: AddElement
) : ElementPayload() {
    override fun elementOp() = ElementOp.ADD_ELEMENT
}


@Serializable
@SerialName("EDIT_ELEMENT")
data class EditElementPayload(
    @SerialName("element")
    override val element: EditElement
) : ElementPayload() {
    override fun elementOp() = ElementOp.EDIT_ELEMENT
}

@Serializable
@SerialName("MOVE_ELEMENT")
data class MoveElementPayload(
    @SerialName("element")
    override val element: MoveElement
) : ElementPayload() {
    override fun elementOp() = ElementOp.MOVE_ELEMENT
}

@Serializable
@SerialName("REMOVE_ELEMENT")
data class RemoveElementPayload(
    @SerialName("element")
    override val element: RemoveElement
) : ElementPayload() {
    override fun elementOp() = ElementOp.REMOVE_ELEMENT
}

// endregion

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
