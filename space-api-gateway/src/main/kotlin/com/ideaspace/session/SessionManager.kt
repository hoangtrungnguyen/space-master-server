package com.ideaspace.session

import com.ideaspace.core.models.ProcessKey
import io.ktor.websocket.*
import io.ktor.websocket.CloseReason.*
import io.lettuce.core.RedisClient
import java.util.*
import java.util.concurrent.ConcurrentHashMap

class SessionManager(
    redisClient: RedisClient
) {

    private val doc2process = ConcurrentHashMap<Long, ConcurrentHashMap<ProcessKey, DocumentConnection>>()

    suspend fun register(key: ProcessKey, connection: DocumentConnection) {
        val process2connection = doc2process.computeIfAbsent(key.docId) {
            ConcurrentHashMap()
        }
        val oldConnection = process2connection.put(key, connection)
        if (oldConnection != null) {
            oldConnection.close(CloseReason(Codes.NORMAL, "Reconnect"))
            println("User ${key.userId} re-connected to document ${key.docId} via window ${key.windowId}. Total connections to this document: ${process2connection.size}.")
        } else {
            println("User ${key.userId} connected to document ${key.docId} via window ${key.windowId}. Total connections to this document: ${process2connection.size}.")
        }
    }

    fun unregister(key: ProcessKey) {
        doc2process[key.docId]?.let { process2connection ->
            val removedConnection = process2connection.remove(key)
            if (removedConnection != null) {
                println("User ${key.userId} disconnected documented ${key.docId} on window ${key.windowId}. Remaining connections: ${process2connection.size}")
            }


            if (process2connection.isEmpty()) {
                doc2process.remove(key.docId)

                // TODO Unsubscribe redis
                println("Connections to document ${key.docId} is now empty and has been removed.")
            }
        }
    }


    fun getConnections(docId: Long): Map<ProcessKey, DocumentConnection>? {
        return doc2process[docId]?.let { Collections.unmodifiableMap(it) }
    }
}

