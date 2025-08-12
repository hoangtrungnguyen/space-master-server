@file:OptIn(ExperimentalTime::class)

package com.ideaspace.core.repository.dto

import com.ideaspace.core.datasources.postgres.entities.DocumentDAO
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import java.util.UUID
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

@Serializable
data class CreateDocumentRequest(
    val name: String
)

object UUIDSerializer : KSerializer<UUID> {
    override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("UUID", PrimitiveKind.STRING)

    override fun serialize(encoder: Encoder, value: UUID) {
        encoder.encodeString(value.toString())
    }

    override fun deserialize(decoder: Decoder): UUID {
        return UUID.fromString(decoder.decodeString())
    }
}


object InstantAsEpochMilliSerializer : KSerializer<Instant> {
    override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("Instant", PrimitiveKind.LONG)

    override fun serialize(encoder: Encoder, value: Instant) {
        encoder.encodeLong(value.toEpochMilliseconds())
    }

    override fun deserialize(decoder: Decoder): Instant {
        return Instant.fromEpochMilliseconds(decoder.decodeLong())
    }
}

@Serializable
data class DocumentDTO(
    val id: Long,
    val title: String,
    @Serializable(with = UUIDSerializer::class)
    val uuid: UUID,
    @Serializable(with = InstantAsEpochMilliSerializer::class)
    val createdAt: Instant
)

fun DocumentDAO.toDTO(): DocumentDTO = DocumentDTO(
    id = this.id.value,
    title = this.title,
    uuid = this.uuid,
    createdAt = this.createdAt
)
