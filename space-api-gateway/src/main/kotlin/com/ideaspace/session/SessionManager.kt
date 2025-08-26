package com.ideaspace.session

import com.ideaspace.core.models.ProcessKey
import io.ktor.websocket.*
import io.ktor.websocket.CloseReason.*
import io.lettuce.core.RedisClient
import io.lettuce.core.api.StatefulRedisConnection
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.Json
import java.util.*
import java.util.concurrent.ConcurrentHashMap

class SessionManager(
    redisClient: RedisClient
) {

    private val redis: StatefulRedisConnection<String, String> by lazy { redisClient.connect() }
    private val redisSubscriber = RedisSubscriber(redisClient, CoroutineScope(Dispatchers.IO + SupervisorJob()))
    private val doc2process = ConcurrentHashMap<Long, ConcurrentHashMap<ProcessKey, DocumentConnection>>()

    suspend fun register(key: ProcessKey, connection: DocumentConnection) {
        val process2connection = doc2process.computeIfAbsent(key.docId) {
            // Subscribe to Redis keyspace notifications for this document when first connection is made
            subscribeToDocumentSyncEvents(key.docId)
            ConcurrentHashMap()
        }
        val oldConnection = process2connection.put(key, connection)
        if (oldConnection != null) {
            oldConnection.close(CloseReason(Codes.NORMAL, "Reconnect"))
            println("User ${key.userId} re-connected to document ${key.docId} via window ${key.windowId}. " +
                    "Total connections to this document: ${process2connection.size}.")
        } else {
            println("User ${key.userId} connected to document ${key.docId} via window ${key.windowId}. " +
                    "Total connections to this document: ${process2connection.size}.")
        }
    }

    fun unregister(key: ProcessKey) {
        doc2process[key.docId]?.let { process2connection ->
            val removedConnection = process2connection.remove(key)
            if (removedConnection != null) {
                println("User ${key.userId} disconnected documented ${key.docId} on window ${key.windowId}. " +
                        "Remaining connections: ${process2connection.size}")
            }


            if (process2connection.isEmpty()) {
                doc2process.remove(key.docId)

                // Unsubscribe from Redis keyspace notifications when no connections remain
                unsubscribeFromDocumentSyncEvents(key.docId)
                println("Connections to document ${key.docId} is now empty and has been removed.")
            }
        }
    }


    fun getConnections(docId: Long): Map<ProcessKey, DocumentConnection>? {
        return doc2process[docId]?.let { Collections.unmodifiableMap(it) }
    }

    /**
     * Subscribe to Redis keyspace notifications for document sync events
     */
    private fun subscribeToDocumentSyncEvents(docId: Long) {
        runBlocking {
            redisSubscriber.subscribeToDocument(docId) { syncEvent ->
                // Handle sync event - broadcast to all connections for this document
                handleSyncEvent(docId, syncEvent)
            }
        }
    }

    /**
     * Unsubscribe from Redis keyspace notifications for document sync events
     */
    private fun unsubscribeFromDocumentSyncEvents(docId: Long) {
        runBlocking {
            redisSubscriber.unsubscribeFromDocument(docId)
        }
    }

    /**
     * Handle incoming sync events from Redis and broadcast to connected clients
     */
    private suspend fun handleSyncEvent(docId: Long, documentMessage: DocumentChannelOutput) {
        val connections = doc2process[docId]
        if (connections != null) {
            val eventText = Json.encodeToString(documentMessage)
            connections.values.forEach { documentConnection ->
                try {
                    documentConnection.webSocket.send(Frame.Text(eventText))
                } catch (e: Exception) {
                    println("❌ Failed to send sync event to connection: ${e.message}")
                }
            }
        }
    }

    /**
     * Clean up all Redis subscriptions and resources
     */
    suspend fun close() {
        redisSubscriber.close()
        redis.close()
    }
}

