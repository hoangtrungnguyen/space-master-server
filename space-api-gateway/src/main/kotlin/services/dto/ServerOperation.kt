package com.space.services.dto

import kotlinx.serialization.Serializable
import org.apache.avro.Schema
import org.apache.avro.generic.GenericData
import org.apache.avro.generic.GenericRecord

@Serializable
data class ServerOperation(
    val operationId: String,
    val boardId: String,
    val userId: String,
    val revision: Int
) {
    companion object {
        private const val SCHEMA_STRING = """
            {
              "type": "record",
              "name": "ServerOperation",
              "namespace": "com.space.services.dto",
              "fields": [
                { "name": "operationId", "type": "string" },
                { "name": "boardId", "type": "string" },
                { "name": "userId", "type": "string" },
                { "name": "revision", "type": "int" }
              ]
            }
        """
        val SCHEMA: Schema = Schema.Parser().parse(SCHEMA_STRING)
    }

    fun toGenericRecord(): GenericRecord {
        return GenericData.Record(SCHEMA).apply {
            put("operationId", operationId)
            put("boardId", boardId)
            put("userId", userId)
            put("revision", revision)
        }
    }
}