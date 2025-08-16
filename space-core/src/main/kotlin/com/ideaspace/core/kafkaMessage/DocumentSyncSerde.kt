package com.ideaspace.core.kafkaMessage

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.decodeFromStream
import kotlinx.serialization.json.encodeToStream
import org.apache.kafka.common.serialization.Deserializer
import org.apache.kafka.common.serialization.Serializer
import java.io.ByteArrayOutputStream

@OptIn(ExperimentalSerializationApi::class)
class DocumentEventSerializer : Serializer<DocumentSyncEventValue> {
    
    private val json = Json {
        ignoreUnknownKeys = true
        encodeDefaults = true
    }
    
    override fun serialize(topic: String?, data: DocumentSyncEventValue?): ByteArray? {
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


@OptIn(ExperimentalSerializationApi::class)
class DocumentEventDeserializer : Deserializer<DocumentSyncEventValue> {

    private val json = Json {
        ignoreUnknownKeys = true
        encodeDefaults = true
    }

    override fun deserialize(topic: String?, data: ByteArray?): DocumentSyncEventValue? {
        return if (data == null) {
            null
        } else {
            try {
                json.decodeFromStream<DocumentSyncEventValue>(data.inputStream())
            } catch (e: Exception) {
                throw RuntimeException("Error deserializing DocumentSyncEventValue", e)
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
