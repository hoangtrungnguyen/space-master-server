package com.space.services

import com.space.services.dto.ServerOperation
import io.github.flaxoos.ktor.server.plugins.kafka.KafkaRecordKey
import org.apache.avro.generic.GenericData
import org.apache.avro.generic.GenericRecord
import org.apache.kafka.clients.producer.KafkaProducer
import org.apache.kafka.clients.producer.ProducerRecord
import kotlin.random.Random


class KafkaProducerService(
    private val producer: KafkaProducer<KafkaRecordKey, GenericRecord>
) {
    suspend fun send(topic: String, message: Any) {
        try {
            producer.send(
                ProducerRecord(
                    "server-operation-topic",
                    ServerOperation(
                        operationId = "a1u2c3d4-e5f6-7890-g1h2-i3j4k5l6m7n8",
                        boardId = "board-98765",
                        userId = "user-12345",
                        revision = 1
                    ).toGenericRecord()
                )
            )
        } catch (e: Exception){
            e.printStackTrace()
        }
    }
}