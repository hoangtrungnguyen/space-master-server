package com.space.session


import java.util.Collections
import java.util.concurrent.ConcurrentHashMap

/**
 * Manages active WebSocket connections for all whiteboard sessions within a single server instance.
 *
 * This class is responsible for tracking which users are connected to which whiteboards at any given moment.
 * It is designed to be thread-safe to handle simultaneous connections and disconnections from multiple users.
 *
 * Its primary role is to act as an in-memory registry for the local server instance. It does NOT handle
 * business logic like user counting for subscription limits; that responsibility is delegated to a
 * dedicated repository to ensure scalability and maintainability.
 */
class SessionManager {

    /**
     * The core data structure for managing sessions. It's a thread-safe map where:
     * - The Key (`String`) is the unique identifier for a whiteboard (`boardId`).
     * - The Value (`MutableSet<Connection>`) is a thread-safe set containing all active
     * [WhiteboardConnection] objects for that specific whiteboard.
     */
    private val sessions = ConcurrentHashMap<String, MutableSet<WhiteboardConnection>>()

    /**
     * Registers a new client connection to a specific whiteboard session.
     *
     * If a session for the given `boardId` does not already exist, this method atomically
     * creates a new session entry.
     *
     * @param connection The [Connection] object representing the newly connected client.
     * @param boardId The unique identifier of the whiteboard the client is joining.
     */
    fun register(connection: WhiteboardConnection, boardId: String) {
        // `computeIfAbsent` is an atomic operation. It ensures that if multiple threads
        // try to register for the same new boardId simultaneously, only one set is created.
        val connections = sessions.computeIfAbsent(boardId) {
            // We use a synchronized set to ensure that additions and removals to the set
            // are thread-safe. `LinkedHashSet` is used to maintain insertion order, though
            // it's not strictly necessary for functionality.
            Collections.synchronizedSet(LinkedHashSet<WhiteboardConnection>())
        }
        connections.add(connection)
        println("Connection registered for board: $boardId. Total connections for this board: ${connections.size}")
    }

    /**
     * Removes a client connection from a whiteboard session, typically upon disconnection.
     *
     * To conserve memory, if this was the last active connection for a given whiteboard,
     * the entire session entry for that `boardId` is removed from the map.
     *
     * @param connection The [Connection] object to be removed.
     * @param boardId The unique identifier of the whiteboard the client is leaving.
     */
    fun unregister(connection: WhiteboardConnection, boardId: String) {
        // We use the safe-call operator `?` to handle cases where the session might already
        // have been removed by another thread.
        sessions[boardId]?.let { connections ->
            val removed = connections.remove(connection)
            if (removed) {
                println("Connection unregistered for board: $boardId. Remaining connections: ${connections.size}")
            }

            // To prevent memory leaks, we clean up the session entry if no connections remain.
            // The `synchronized` block ensures that we don't accidentally remove a session
            // while another thread is trying to add a new connection to it.
            if (connections.isEmpty()) {
                sessions.remove(boardId)
                println("Session for board $boardId is now empty and has been removed.")
            }
        }
    }

    /**
     * Retrieves an immutable list of all active connections for a given whiteboard.
     *
     * This method is primarily used to broadcast messages or events to all participants
     * currently active in a specific whiteboard session.
     *
     * @param boardId The unique identifier of the whiteboard session.
     * @return A `List<Connection>` containing all connections for the board.
     * Returns an empty list if the session does not exist or has no active connections.
     */
    fun getConnections(boardId: String): List<WhiteboardConnection> {
        // Returning a new list copy (`toList()`) prevents external code from modifying the
        // internal set and avoids ConcurrentModificationException if the set is changed
        // while another thread is iterating over the list.
        return sessions[boardId]?.toList() ?: emptyList()
    }
}

