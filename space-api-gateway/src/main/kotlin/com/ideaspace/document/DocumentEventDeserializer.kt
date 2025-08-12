package com.ideaspace.document

import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import org.apache.kafka.common.serialization.Deserializer

class DocumentEventDeserializer : Deserializer<DocumentEvent> {
    
    private val json = Json {
        ignoreUnknownKeys = true
        encodeDefaults = true
    }
    
    override fun deserialize(topic: String?, data: ByteArray?): DocumentEvent? {
        return if (data == null) {
            null
        } else {
            try {
                val jsonString = String(data, Charsets.UTF_8)
                json.decodeFromString<DocumentEvent>(jsonString)
            } catch (e: Exception) {
                throw RuntimeException("Error deserializing DocumentEvent", e)
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
