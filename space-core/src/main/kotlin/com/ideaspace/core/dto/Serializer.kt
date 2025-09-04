@file:OptIn(ExperimentalTime::class)

package com.ideaspace.core.dto

import kotlinx.datetime.format
import kotlinx.datetime.format.DateTimeComponents.Formats.ISO_DATE_TIME_OFFSET
import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
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


object NullableUUIDSerializer : KSerializer<UUID?> {
    override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("NullableUUID", PrimitiveKind.STRING)

    override fun serialize(encoder: Encoder, value: UUID?) {
        if (value != null) {
            encoder.encodeString(value.toString())
        } else {
            encoder.encodeString("") // or encode null
        }
    }

    override fun deserialize(decoder: Decoder): UUID? {
        val stringValue = decoder.decodeString()
        return if (stringValue.isEmpty()) {
            null
        } else {
            UUID.fromString(stringValue)
        }
    }
}