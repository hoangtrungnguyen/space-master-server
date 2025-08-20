package com.ideaspace.core.utils

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.encodeToJsonElement
import kotlinx.serialization.json.encodeToStream
import org.apache.kafka.common.serialization.Serializer
import java.io.ByteArrayOutputStream
import java.time.Instant

/**
 * A data class for structured logging.
 *
 * @property timestamp The time at which the log event occurred.
 * @property level The severity of the log event (e.g., INFO, ERROR).
 * @property loggerName The name of the logger that created the event.
 * @property message A human-readable message describing the event.
 * @property userId The ID of the user associated with the event, if any.
 * @property docId The ID of the document associated with the event, if any.
 * @property processId The ID of the process associated with the event, if any.
 * @property exceptionInfo Information about any exception that occurred.
 */
@Serializable
data class LogData(
    val timestamp: Long = Instant.now().toEpochMilli(),
    val level: String? = null,
    val loggerName: String,
    val message: JsonElement,
    val userId: Long? = null,
    val docId: Long? = null,
    val processId: Long? = null,
    val exceptionInfo: String? = null
){
    fun toJsonElement() = Json.encodeToJsonElement<LogData>(this)
}

@OptIn(ExperimentalSerializationApi::class)
class LogDataEventSerializer : Serializer<LogData> {

    private val json = Json {
        ignoreUnknownKeys = true
        encodeDefaults = true
    }

    override fun serialize(topic: String?, data: LogData?): ByteArray? {
        return if (data == null) {
            null
        } else {
            try {
                ByteArrayOutputStream().use { outputStream ->
                    json.encodeToStream(data, outputStream)
                    outputStream.toByteArray()
                }
            } catch (e: Exception) {
                throw RuntimeException("Error serializing DocumentSyncEventValue", e)
            }
        }
    }

    override fun close() {
        // Nothing to close
    }

    override fun configure(configs: MutableMap<String, *>?, isKey: Boolean) {
        // No configuration needed
    }
}