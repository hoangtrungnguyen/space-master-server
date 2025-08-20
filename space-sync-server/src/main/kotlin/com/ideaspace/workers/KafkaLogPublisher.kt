package com.ideaspace.workers

import com.ideaspace.core.kafkaMessage.DocumentSyncEventValue
import com.ideaspace.core.utils.LogData
import io.netty.handler.logging.LogLevel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.future.await
import kotlinx.coroutines.runBlocking
import org.apache.kafka.clients.producer.KafkaProducer
import org.apache.kafka.clients.producer.ProducerRecord
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.util.concurrent.CompletableFuture
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

interface LogPublisher {

    val logger: Logger get() = LoggerFactory.getLogger(LogPublisher::class.java)

    suspend fun sendEvent(event: LogData) {
        throw Exception("No implement")
    }

    fun info(
        event: LogData? = null,
        message: String? = null,
        toLogServer: Boolean = false
    ) {
        logger.info(message ?: event.toString())
        if (toLogServer) {
            runBlocking(Dispatchers.IO) {
                sendEvent(event!!.copy(level =LogLevel.INFO.name))
            }
        }
    }

    fun warn(
        event: LogData? = null,
        message: String? = null,
        toLogServer: Boolean = false
    ) {
        logger.warn(message ?: event.toString())
        if (toLogServer) {
            runBlocking(Dispatchers.IO) {
                sendEvent(event!!.copy(level =LogLevel.WARN.name))
            }
        }
    }

    suspend fun close() {}
}

class KafkaLogPublisher(
    private val topic: String = "server-log",
    private val kafkaProducer: KafkaProducer<Long, LogData>
) : LogPublisher {

    override val logger: Logger get() = LoggerFactory.getLogger(KafkaLogPublisher::class.java)

    override suspend fun sendEvent(event: LogData) {
        val key = generateNanoTimestampId()
        val record = ProducerRecord<Long, LogData>(topic, key, event)
        val future = CompletableFuture<Unit>()
        kafkaProducer.send(record) { metadata, exception ->
            if (exception != null) {
                logger.error("Failed to send document event", exception)
                future.completeExceptionally(exception)
            } else {
                future.complete(Unit)
            }
        }

        future.await()
    }

    @OptIn(ExperimentalTime::class)
    private fun generateNanoTimestampId(): Long {
        val now = Clock.System.now()
        return now.epochSeconds * 1_000_000_000L + now.nanosecondsOfSecond
    }

    override suspend fun close() {
        kafkaProducer.flush()
        kafkaProducer.close()
    }
}
