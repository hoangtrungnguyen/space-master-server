@file:OptIn(ExperimentalUuidApi::class, ExperimentalUuidApi::class)

package com.ideaspace.core.kafkaMessage

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonObject
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid


@Serializable
data class DocumentSyncEventKey(
    @SerialName("doc_id")
    val docId: String
)
@Serializable
data class DocumentSyncEventValue(
    @SerialName("sync_op")
    val syncOp: SyncOperation,

    @SerialName("doc_id")
    val docId: String,

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
    SAVE_DOC
}

@Serializable
data class ElementPayload(
    @SerialName("element_op")
    val elementOp: SyncOperation,
    @SerialName("element")
    val element: Element
)

@Serializable
data class Element(
    val uuid: Uuid,
    @SerialName("parent_uuid")
    val parentUuid: Uuid? = null,
    val metadata: JsonObject = JsonObject(emptyMap()),
    val type: String,
    val value: JsonObject
)

@Serializable
enum class ElementOp{
    EDIT_ELEMENT,
    MOVE_ELEMENT,
    REMOVE_ELEMENT
}