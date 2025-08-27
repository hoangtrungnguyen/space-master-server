package com.ideaspace.rtcmanager

import io.ktor.server.routing.*
import io.ktor.server.websocket.*
import io.ktor.websocket.*
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import java.util.*

// Data class for WebSocket messages to send lists of peers
@Serializable
data class PeerMessage(
    val type: String,
    val payload: List<String>
)

// Store active connections and their associated Peer IDs
val activePeers = Collections.synchronizedMap<String, DefaultWebSocketSession>(LinkedHashMap())

fun Route.peerSignaling() {
    webSocket("/ws/peer-signaling") {
        val session = this
        try {
            // Read the first message from the client to get their Peer ID
            val message = (incoming.receive() as Frame.Text).readText()
            println("Message: $message")
            val myPeerId = message.split(":")[1]
            println("New peer connected with ID: $myPeerId")

            // Get a list of all current peer IDs except the new one
            val existingPeers = activePeers.keys.filter { it != myPeerId }

            // 1. Send the list of existing peers to the new client
            val existingPeersMessage = Json.encodeToString(PeerMessage("PEER_LIST", existingPeers))
            session.send(existingPeersMessage)

            // 2. Add the new peer to the map
            activePeers[myPeerId] = session

            // 3. Notify all existing peers about the new client
            val newPeerJoinedMessage = Json.encodeToString(PeerMessage("NEW_PEER_JOINED", listOf(myPeerId)))
            activePeers.values.filter { it != session }.forEach { it.send(newPeerJoinedMessage) }

            // Keep the connection open until it's closed
            for (frame in incoming) {
                // Future logic for handling other messages from clients
            }
        } catch (e: Exception) {
            println("Peer signaling error: ${e.localizedMessage}")
        } finally {
            // Clean up when the connection is closed
            val closedPeerId = activePeers.entries.find { it.value == session }?.key
            if (closedPeerId != null) {
                activePeers.remove(closedPeerId)
                println("Unregistered peer with ID: $closedPeerId")
                // Future: Notify remaining peers that this peer has left
            }
        }
    }
}