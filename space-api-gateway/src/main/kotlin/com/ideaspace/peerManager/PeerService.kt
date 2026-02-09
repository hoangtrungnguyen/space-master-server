package com.ideaspace.peerManager

import com.ideaspace.session.*
import java.util.*
import java.util.concurrent.ConcurrentHashMap

typealias PeerUuid = UUID

interface RTCPeerManager {
    suspend fun registerPeerGroup(docId: Long, peerUuid: PeerUuid)
    suspend fun unregisterPeerGroup(docId: Long, peerUuid: PeerUuid)
    suspend fun sendSignal(docId: Long, sourcePeerUuid: PeerUuid, signal: WebRTCSignalInput)
    suspend fun close()
}


class InMemoryPeerManager(
    private val sessionManager: SessionManager,
) : RTCPeerManager {

    private val peers = ConcurrentHashMap<Long, MutableSet<PeerUuid>>()

    override suspend fun registerPeerGroup(docId: Long, peerUuid: PeerUuid) {
        var added = false
        peers.compute(docId) { _, existingSet ->
            val set = existingSet ?: ConcurrentHashMap.newKeySet()
            added = set.add(peerUuid)
            set
        }

        // Broadcast regardless of whether it was added (to ensure sync),
        // but typically only needed if added or if we want to send full list to the joiner.
        broadcastListPeer(docId, newPeer = peerUuid)
    }

    override suspend fun unregisterPeerGroup(docId: Long, peerUuid: PeerUuid) {
        var removed = false
        peers.compute(docId) { _, docPeers ->
            if (docPeers != null) {
                removed = docPeers.remove(peerUuid)
                if (docPeers.isEmpty()) null else docPeers
            } else {
                null
            }
        }

        if (removed) {
            broadcastListPeer(docId, removedPeer = peerUuid)
        }
    }

    override suspend fun sendSignal(docId: Long, sourcePeerUuid: PeerUuid, signal: WebRTCSignalInput) {
        val connections = sessionManager.getConnections(docId) ?: return

        // We need to find the connection associated with the target peer UUID.
        // sessionManager stores connections keyed by ProcessKey (which includes userId, windowId).
        // The Process object inside DocumentConnection has the peerUuid (we need to make sure it's set).

        connections.values.forEach { conn ->
            if (conn.process.peerUuid == signal.targetPeerUuid) {
                try {
                    conn.send(
                        WebRTCSignalOutput(
                            sourcePeerUuid = sourcePeerUuid,
                            signalType = signal.signalType,
                            payload = signal.payload
                        )
                    )
                } catch (e: Exception) {
                    println("Failed to send signal from $sourcePeerUuid to ${signal.targetPeerUuid}: ${e.message}")
                }
            }
        }
    }

    private suspend fun broadcastListPeer(docId: Long, newPeer: PeerUuid? = null, removedPeer: PeerUuid? = null) {
        val currentList = peers[docId]?.toList() ?: emptyList()
        val connections = sessionManager.getConnections(docId) ?: return

        val output = ListPeerOut(
            peerCount = currentList.size,
            newPeer = newPeer,
            removedPeer = removedPeer,
            listPeer = currentList
        )

        connections.values.forEach { conn ->
            try {
                conn.send(output)
            } catch (e: Exception) {
                println("Failed to broadcast peer list: ${e.message}")
            }
        }
    }

    override suspend fun close() {
        peers.clear()
    }

}
