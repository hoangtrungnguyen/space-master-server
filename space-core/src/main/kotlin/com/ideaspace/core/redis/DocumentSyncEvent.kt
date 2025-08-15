package com.ideaspace.core.redis

// Ensure you have the kotlinx.serialization dependency in build.gradle.kts
// implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.3")

import kotlinx.serialization.*
import kotlinx.serialization.json.JsonElement

@Serializable
data class RedisSyncOperation(
    @SerialName("sync_op")
    val syncOp: String,

    @SerialName("doc_id")
    val docId: Long,

    @SerialName("process_id")
    val processId: Long,

    @SerialName("user_id")
    val userId: Int,

    @SerialName("session_id")
    val sessionId: Long,

    @SerialName("client_id")
    val clientId: Int,

    val payload: Payload
)

@Serializable
data class Payload(
    @SerialName("element_op")
    val elementOp: String,
    val element: Element
)

@Serializable
data class Element(
    val uuid: String,
    @SerialName("parent_uuid")
    val parentUuid: String?,
    val metadata: JsonElement,
    val type: String, // e.g., "HEADING", "TEXT_BLOCK", "SHAPE"
    val value:  JsonElement // Or a more generic type if needed, see note below
)


