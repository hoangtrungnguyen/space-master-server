package com.ideaspace.session

import com.ideaspace.core.models.Process
import io.ktor.websocket.*
import kotlinx.coroutines.isActive
import kotlinx.serialization.json.Json


data class DocumentConnection(
    val process: Process,
    val webSocket: DefaultWebSocketSession
) {
    suspend inline fun <reified T> send(data: T) {
        // Only attempt to send if the underlying WebSocket session is active.
        if (webSocket.isActive) {
            try {
                // Serialize the data object to its JSON string representation.
                val jsonString = Json.encodeToString(data)
                // Send the JSON string as a WebSocket Text frame.
                webSocket.send(Frame.Text(jsonString))
            } catch (e: Exception) {
                // Log any exceptions that occur during serialization or sending.
                // This could be a kotlinx.serialization.SerializationException or an I/O error.
                println("Error sending data to user ${process.userId} at document ${process.docId} in window ${process.windowId}: ${e.message}")
                // Depending on the error, you might want to close the connection.
            }
        }
    }

    suspend fun close(reason: CloseReason) {
        webSocket.close(reason)
    }
}
