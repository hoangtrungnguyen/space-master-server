package com.ideaspace.core.kafkaMessage

import kotlinx.serialization.json.Json
import org.apache.kafka.common.serialization.Deserializer

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
                val jsonString = String(data, Charsets.UTF_8)
                json.decodeFromString<DocumentSyncEventValue>(jsonString)
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

