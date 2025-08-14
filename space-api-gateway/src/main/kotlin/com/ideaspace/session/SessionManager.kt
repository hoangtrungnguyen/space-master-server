package com.ideaspace.session


import java.util.Collections
import java.util.concurrent.ConcurrentHashMap

class SessionManager {

    private val sessions = ConcurrentHashMap<String, MutableSet<DocumentConnection>>()

    fun register(connection: DocumentConnection, docId: String) {
        val connections = sessions.computeIfAbsent(docId) {
            Collections.synchronizedSet(LinkedHashSet<DocumentConnection>())
        }
        connections.add(connection)
        println("Connection registered for board: $docId. Total connections for this board: ${connections.size}")
    }


    fun unregister(connection: DocumentConnection, boardId: String) {
        sessions[boardId]?.let { connections ->
            val removed = connections.remove(connection)
            if (removed) {
                println("Connection unregistered for board: $boardId. Remaining connections: ${connections.size}")
            }


            if (connections.isEmpty()) {
                sessions.remove(boardId)
                println("Session for board $boardId is now empty and has been removed.")
            }
        }
    }


    fun getConnections(boardId: String): List<DocumentConnection> {
        return sessions[boardId]?.toList() ?: emptyList()
    }
}

