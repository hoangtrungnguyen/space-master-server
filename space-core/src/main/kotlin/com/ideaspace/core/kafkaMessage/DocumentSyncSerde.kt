package com.ideaspace.core.kafkaMessage

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.KSerializer
import kotlinx.serialization.builtins.MapSerializer
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.decodeFromStream
import kotlinx.serialization.json.encodeToStream
import org.apache.kafka.common.serialization.Deserializer
import org.apache.kafka.common.serialization.Serializer
import java.io.ByteArrayOutputStream
import java.util.concurrent.ConcurrentHashMap

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


class ConcurrentHashMapSerializer<K : Any, V : Any>(
    private val keySerializer: KSerializer<K>,
    private val valueSerializer: KSerializer<V>
) : KSerializer<ConcurrentHashMap<K, V>> {

    // Delegate the descriptor to the MapSerializer
    private val mapSerializer = MapSerializer(keySerializer, valueSerializer)

    override val descriptor: SerialDescriptor = mapSerializer.descriptor

    override fun serialize(encoder: Encoder, value: ConcurrentHashMap<K, V>) {
        // The serialization logic is the same as for a regular map
        mapSerializer.serialize(encoder, value)
    }

    override fun deserialize(decoder: Decoder): ConcurrentHashMap<K, V> {
        // Deserialize as a regular map and then convert to a ConcurrentHashMap
        return ConcurrentHashMap(mapSerializer.deserialize(decoder))
    }
}