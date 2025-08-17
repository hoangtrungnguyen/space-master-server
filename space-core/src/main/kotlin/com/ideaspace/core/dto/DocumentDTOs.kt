@file:OptIn(ExperimentalTime::class)

package com.ideaspace.core.dto

import com.ideaspace.core.dao.DocumentDAO
import com.ideaspace.core.models.DocumentStatus
import com.ideaspace.core.models.DocumentType
import com.ideaspace.core.models.Element
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement
import java.util.*
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

@Serializable
data class DocumentDTO(
    val id: Long,
    @Serializable(with = UUIDToString::class)
    val uuid: UUID,
    val revId: Long,
    val title: String,

    // TODO Replace with user info
    val creatorId: Long,
    val ownerId: Long,
    @Serializable(with = InstantToISODateTime::class)
    val createdAt: Instant,
    @Serializable(with = InstantToISODateTime::class)
    val lastModifiedAt: Instant,
    val metadata: JsonElement?,
    val documentType: DocumentType,
    val status: DocumentStatus,
    val content: ElementDTO? = null
)

fun DocumentDAO.toDTO(): DocumentDTO = DocumentDTO(
    id = this.id.value,
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
)

fun toDTO(document: DocumentDAO, root: Element): DocumentDTO = DocumentDTO(
    id = document.id.value,
    uuid = document.uuid,
    revId = document.revId,
    title = document.title,
    creatorId = document.creatorId,
    ownerId = document.ownerId,
    createdAt = document.createdAt,
    lastModifiedAt = document.lastModifiedAt,
    metadata = document.metadata,
    documentType = document.documentType,
    status = document.status,
    content = root.toDTO()
)

