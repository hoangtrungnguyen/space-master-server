package com.ideaspace.core.kafkaMessage

import kotlinx.serialization.json.Json
import org.apache.kafka.common.serialization.Serializer

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
                json.encodeToString(data).toByteArray(Charsets.UTF_8)
            } catch (e: Exception) {
                throw RuntimeException("Error serializing DocumentEvent", e)
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

