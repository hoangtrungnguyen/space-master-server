@file:OptIn(ExperimentalTime::class)

package com.ideaspace.core.models

import com.ideaspace.core.dto.DocumentDTO
import com.ideaspace.core.dto.toDTO
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
    val latestRedisEntry: String
){
    val roots : Map<UUID,Element> = emptyMap<UUID,Element>()
    val elements: Map<UUID, Element> = emptyMap()
}

@Serializable
enum class DocumentStatus {
    DRAFT, PUBLISHED, ARCHIVED,
}

@Serializable
enum class DocumentType {
    DOC, CANVAS
}

fun BusinessDocument.toDTO(elements: List<Element>): DocumentDTO = DocumentDTO(
    id = this.id,
    uuid = this.uuid,
    revId = this.revId,
    title = this.title,
    creatorId = this.creatorId,
    ownerId = this.ownerId,
    createdAt = this.createdAt,
    lastModifiedAt = this.lastModifiedAt,
    metadata = this.metadata,
    documentType = this.documentType,
    status = this.status,
    streamCursor = "(" + this.latestRedisEntry,
    elements = elements.map { it.toDTO() }
)