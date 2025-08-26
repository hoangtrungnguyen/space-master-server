@file:OptIn(ExperimentalUuidApi::class, ExperimentalUuidApi::class, ExperimentalSerializationApi::class)

package com.ideaspace.core.redis

import com.ideaspace.core.dto.UUIDToString
import com.ideaspace.core.kafkaMessage.*
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonClassDiscriminator
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import java.util.*
import kotlin.uuid.ExperimentalUuidApi

@Serializable
@JsonClassDiscriminator("sync_op")
sealed class RedisDocumentEvent {
    abstract val docId: Long
}

@Serializable
@SerialName("INIT_SYNC")
data class InitSyncEvent(
    override val docId: Long,
    val syncOp: SyncOperation = SyncOperation.INIT_SYNC,
    val processId: Long,
    val userId: Long,
    val windowId: Long,
) : RedisDocumentEvent()

@Serializable
@SerialName("EDIT_DOC")
data class EditDocEvent(
    override val docId: Long,
    val syncOp: SyncOperation = SyncOperation.EDIT_DOC,
    val processId: Long,
    val userId: Long,
    val windowId: Long,
    val payload: RedisEditDocPayload
) : RedisDocumentEvent()

@Serializable
@SerialName("SAVE_DOC")
data class RedisSaveDocEvent(
    override val docId: Long,
    val syncOp: SyncOperation = SyncOperation.SAVE_DOC,
    val processId: Long,
    val userId: Long,
    val windowId: Long,
    val payload: RedisSaveDocPayload
) : RedisDocumentEvent()

@Serializable
@SerialName("FINISH_SYNC")
data class RedisFinishSyncEvent(
    override val docId: Long,
    val syncOp: SyncOperation = SyncOperation.FINISH_SYNC,
    val processId: Long,
    val userId: Long,
    val windowId: Long,
    val payload: RedisFinishSyncPayload
) : RedisDocumentEvent()

sealed class RedisDocumentSyncPayload

@Serializable
class RedisInitSyncPayload : RedisDocumentSyncPayload()

@Serializable
@JsonClassDiscriminator("element_op")
sealed class RedisEditDocPayload : RedisDocumentSyncPayload() {
    abstract val elementOp: ElementOp
    abstract val element: RedisElement
}

@Serializable
class RedisSaveDocPayload : RedisDocumentSyncPayload()

@Serializable
class RedisFinishSyncPayload : RedisDocumentSyncPayload()

@Serializable
@SerialName("ADD_ELEMENT")
data class RedisAddElementPayload(
    override val elementOp: ElementOp = ElementOp.ADD_ELEMENT,
    override val element: RedisAddElement
) : RedisEditDocPayload()

@Serializable
@SerialName("EDIT_ELEMENT")
data class RedisEditElementPayload(
    override val elementOp: ElementOp = ElementOp.EDIT_ELEMENT,
    override val element: RedisEditElement
) : RedisEditDocPayload()

@Serializable
@SerialName("MOVE_ELEMENT")
data class RedisMoveElementPayload(
    override val elementOp: ElementOp = ElementOp.MOVE_ELEMENT,
    override val element: RedisMoveElement
) : RedisEditDocPayload()

@Serializable
@SerialName("REMOVE_ELEMENT")
data class RedisRemoveElementPayload(
    override val elementOp: ElementOp = ElementOp.REMOVE_ELEMENT,
    override val element: RedisRemoveElement
) : RedisEditDocPayload()

@Serializable
sealed class RedisElement {
    abstract val uuid: UUID
}

@Serializable
data class RedisAddElement(
    @Serializable(with = UUIDToString::class)
    override val uuid: UUID,
    @SerialName("parent_uuid") @Serializable(with = UUIDToString::class)
    val parentUuid: UUID? = null,
    val metadata: JsonElement?,
    val type: String,
    val value: JsonElement
) : RedisElement()

@Serializable
data class RedisEditElement(
    @Serializable(with = UUIDToString::class)
    override val uuid: UUID,
    val metadata: JsonElement?,
    val type: String,
    val value: JsonElement
) : RedisElement()

@Serializable
data class RedisMoveElement(
    @Serializable(with = UUIDToString::class)
    override val uuid: UUID,
    @SerialName("parent_uuid") @Serializable(with = UUIDToString::class)
    val parentUuid: UUID? = null,
) : RedisElement()

@Serializable
data class RedisRemoveElement(
    @Serializable(with = UUIDToString::class)
    override val uuid: UUID,
) : RedisElement()

fun DocumentSyncEventValue.toRedisDocumentEvent(): RedisDocumentEvent {
    return when (this) {
        is InitSyncEventValue -> InitSyncEvent(
            docId = this.docId,
            processId = this.processId,
            userId = this.userId,
            windowId = this.windowId,
        )
        is EditDocEventValue -> EditDocEvent(
            docId = this.docId,
            processId = this.processId,
            userId = this.userId,
            windowId = this.windowId,
            payload = this.payload.toRedisEditPayLoad())
        is SaveDocEventValue -> RedisSaveDocEvent(
            docId = this.docId,
            processId = this.processId,
            userId = this.userId,
            windowId = this.windowId,
            payload = RedisSaveDocPayload()
        )
        is FinishSyncEventValue -> RedisFinishSyncEvent(
            docId = this.docId,
            processId = this.processId,
            userId = this.userId,
            windowId = this.windowId,
            payload = RedisFinishSyncPayload()
        )
        else -> throw RuntimeException("Missing case handling ${this.javaClass}")
    }
}
fun EditDocPayload.toRedisEditPayLoad(): RedisEditDocPayload {
    return when (val p = this) {
        is AddElementPayload -> RedisAddElementPayload(
            element = RedisAddElement(
                uuid = p.element.uuid,
                parentUuid = p.element.parentUuid,
                metadata = p.element.metadata,
                type = p.element.type,
                value = p.element.value
            )
        )
        is EditElementPayload -> RedisEditElementPayload(
            element = RedisEditElement(
                uuid = p.element.uuid,
                metadata = p.element.metadata,
                type = p.element.type,
                value = p.element.value
            )
        )
        is MoveElementPayload -> RedisMoveElementPayload(
            element = RedisMoveElement(
                uuid = p.element.uuid,
                parentUuid = p.element.parentUuid
            )
        )
        is RemoveElementPayload -> RedisRemoveElementPayload(
            element = RedisRemoveElement(
                uuid = p.element.uuid
            )
        )
    }
}


fun redisDocKey(docId: Long): String {
    return "document:$docId"
}

fun redisDocSyncEventsKey(docId: Long): String {
    return "ideaspace:doc:$docId:stream"
}