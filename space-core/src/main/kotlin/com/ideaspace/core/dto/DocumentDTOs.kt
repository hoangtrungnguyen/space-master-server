@file:OptIn(ExperimentalTime::class)

package com.ideaspace.core.dto

//import com.ideaspace.core.InstantAsEpochMilliSerializer
//import com.ideaspace.core.UUIDSerializer
import com.ideaspace.core.dao.DocumentDAO
import com.ideaspace.core.repository.dto.InstantToISODateTime
import com.ideaspace.core.repository.dto.UUIDToString
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


@Serializable
data class DocumentDTO(
    val id: Long,
    val title: String,
    @Serializable(with = UUIDToString::class)
    val uuid: UUID,
    @Serializable(with = InstantToISODateTime::class)
    val createdAt: Instant
)

fun DocumentDAO.toDTO(): DocumentDTO = DocumentDTO(
    id = this.id.value,
    title = this.title,
    uuid = this.uuid,
    createdAt = this.createdAt
)
