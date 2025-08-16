package com.ideaspace.session

import io.ktor.websocket.*
import kotlinx.coroutines.isActive
import kotlinx.serialization.json.Json
import java.util.*

data class DocumentConnection(
    val userId: Long,
    val docUuid: UUID,
    val session: DefaultWebSocketSession
) {
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
                println("Error sending data to session # for user $userId: ${e.message}")
                // Depending on the error, you might want to close the connection.
            }
        }
    }
}
