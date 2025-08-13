package com.ideaspace.core.models

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement
import java.util.*
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

@OptIn(ExperimentalTime::class)
class BusinessDocument(
    val id: Long,
    val uuid: UUID,
    val revId: Long,
    val title: String,
    val creatorId: Long,
    val ownerId: Long,
    val createdAt: Instant,
    val lastModifiedAt: Instant,
    val metadata: JsonElement?,
    val documentType: DocumentType,
    val status: DocumentStatus,
    val transformVersion: Long,
    val kafkaOffset: Long,
)

@Serializable
enum class DocumentStatus {
    DRAFT, PUBLISHED, ARCHIVED,
}

@Serializable
enum class DocumentType {
    DOC, CANVAS
}