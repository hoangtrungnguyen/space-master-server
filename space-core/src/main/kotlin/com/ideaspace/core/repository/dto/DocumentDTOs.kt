@file:OptIn(ExperimentalTime::class)

package com.ideaspace.core.repository.dto

import com.ideaspace.core.dao.DocumentDAO
import com.ideaspace.core.models.DocumentStatus
import com.ideaspace.core.models.DocumentType
import kotlinx.datetime.format
import kotlinx.datetime.format.DateTimeComponents.Formats.ISO_DATE_TIME_OFFSET
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.JsonElement
import java.util.*
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

object UUIDToString : KSerializer<UUID> {
    override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("UUID", PrimitiveKind.STRING)

    override fun serialize(encoder: Encoder, value: UUID) {
        encoder.encodeString(value.toString())
    }

    override fun deserialize(decoder: Decoder): UUID {
        return UUID.fromString(decoder.decodeString())
    }
}


object InstantToISODateTime : KSerializer<Instant> {
    override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("Instant", PrimitiveKind.STRING)

    override fun serialize(encoder: Encoder, value: Instant) {
        encoder.encodeString(value.format(ISO_DATE_TIME_OFFSET))
    }

    override fun deserialize(decoder: Decoder): Instant {
        return Instant.parse(decoder.decodeString())
    }
}

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
    status = this.status
)
