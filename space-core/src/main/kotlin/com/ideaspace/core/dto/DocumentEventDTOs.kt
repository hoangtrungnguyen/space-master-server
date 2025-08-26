package com.ideaspace.core.dto

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import java.util.*


@Serializable
@OptIn(ExperimentalSerializationApi::class)
data class DocumentEventDTO(
    @SerialName("sync_op")
    val syncOp: SyncOperation,

    @SerialName("doc_id")
    val docId: Long? = null,

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
data class ElementPayload(
    @SerialName("element_op")
    val elementOp: ElementOp,
    @SerialName("element")
    val element: Element
)

@Serializable
data class Element(
    @Serializable(with = UUIDToString::class)
    val uuid: UUID,
    @SerialName("parent_uuid")
    @Serializable(with = UUIDToString::class)
    val parentUuid: UUID? = null,
    val metadata: JsonElement = JsonObject(emptyMap()),
    val type: String,
    val value: JsonElement
)

@Serializable
enum class ElementOp{
    ADD_ELEMENT,
    EDIT_ELEMENT,
    MOVE_ELEMENT,
    REMOVE_ELEMENT
}
