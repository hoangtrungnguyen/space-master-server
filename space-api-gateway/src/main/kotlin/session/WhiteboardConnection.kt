package com.space.session

import io.ktor.websocket.*
import kotlinx.coroutines.isActive
import kotlinx.serialization.json.Json
import java.util.*

/**
 * Represents a single client connection to the server for a whiteboard session.
 *
 * This data class encapsulates all necessary information about a user's WebSocket session,
 * including a unique identifier for the connection, the user's own ID, and the underlying
 * Ktor WebSocket session object used for communication.
 *
 * Using this class simplifies session management by allowing handlers and services to operate on a
 * single, cohesive object rather than passing around multiple parameters like userId and the session itself.
 * It makes the code cleaner and more object-oriented.
 *
 * @property sessionId A unique identifier for this specific connection instance, generated automatically.
 * @property userId The identifier for the user who owns this connection. This is crucial for tracking and authorization.
 * @property session The underlying [DefaultWebSocketSession] from Ktor, used to send and receive WebSocket frames.
 */
data class WhiteboardConnection(
    val sessionId: String = UUID.randomUUID().toString(),
    val userId: String,
    val session: DefaultWebSocketSession
) {

    /**
     * A utility function to serialize an object to a JSON string and send it over the WebSocket.
     *
     * This helper method abstracts away the serialization logic, making the handler code cleaner and
     * less repetitive. It includes a check to ensure the session is still active before attempting to send,
     * preventing errors on closed connections.
     *
     * @param T The type of the object to be sent. Must be serializable by kotlinx.serialization.
     * @param data The object to serialize and send.
     */
    suspend inline fun <reified T> send(data: T) {
        // Only attempt to send if the underlying WebSocket session is active.
        if (session.isActive) {
            try {
                // Serialize the data object to its JSON string representation.
                val jsonString = Json.encodeToString(data)
                // Send the JSON string as a WebSocket Text frame.
                session.send(Frame.Text(jsonString))
            } catch (e: Exception) {
                // Log any exceptions that occur during serialization or sending.
                // This could be a kotlinx.serialization.SerializationException or an I/O error.
                println("Error sending data to session $sessionId for user $userId: ${e.message}")
                // Depending on the error, you might want to close the connection.
            }
        }
    }
}
