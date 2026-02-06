package com.ideaspace.peerManager

import com.ideaspace.core.dto.UUIDToString
import com.ideaspace.core.models.Process
import com.ideaspace.session.ListPeerOut
import com.ideaspace.session.SessionManager
import io.lettuce.core.RedisClient
import kotlinx.coroutines.CoroutineScope
import kotlinx.serialization.Serializable
import java.util.*
import java.util.concurrent.ConcurrentHashMap

@Serializable
data class PeerData(
    val process: Process,
    @Serializable(with = UUIDToString::class)
    val peerUuid: UUID
)

typealias PeerUuid = UUID

interface RTCPeerManager {
    suspend fun registerPeerGroup(docId: Long, peerUuid: PeerUuid)
    suspend fun unregisterPeerGroup(docId: Long, peerUuid: PeerUuid)
    suspend fun close()
}


class RedisPeerManagerImpl(
    private val sessionManager: SessionManager,
    private val redisClient: RedisClient,
    private val scope: CoroutineScope,
) : RTCPeerManager {

    private val peerRedisSubscriber = PeerRedisSubscriber(redisClient, scope)
    
    // Track active subscriptions for this instance to avoid redundant subscribe calls
    private val activeSubscriptions = ConcurrentHashMap.newKeySet<Long>()

    override suspend fun registerPeerGroup(docId: Long, peerUuid: PeerUuid) {
        // 1. Subscribe to events for this document if not already subscribed
        if (activeSubscriptions.add(docId)) {
            peerRedisSubscriber.subscribeToPeerGroup(docId) { event ->
                handlePeerEvent(event)
            }
        }

        // 2. Join the group in Redis and get the current full list of peers
        val currentPeers = peerRedisSubscriber.joinAndNotify(docId, peerUuid)

        // 3. Send the FULL LIST to the joining peer ONLY
        // We find the connection(s) belonging to this peerUuid
        sessionManager.getConnections(docId)?.forEach { (_, conn) ->
            if (conn.process.peerUuid == peerUuid) {
                try {
                    conn.send(
                        ListPeerOut(
                            peerCount = currentPeers.size,
                            listPeer = currentPeers,
                            newPeer = peerUuid // Optional: indicate self-join
                        )
                    )
                } catch (e: Exception) {
                    println("Failed to send initial peer list to $peerUuid: ${e.message}")
                }
            }
        }
    }

    private suspend fun handlePeerEvent(event: PeerEvent) {
        // Broadcast the event to all LOCAL connections for this document
        val fullList = peerRedisSubscriber.getPeers(event.docId)
        
        sessionManager.getConnections(event.docId)?.forEach { (key, conn) ->
            // Optionally, avoid echoing back to the sender if the event originated from them
            // But usually, it's safer to let frontend handle "I joined" confirmation unless we want to filter.
            // For now, we broadcast to everyone so they stay in sync.
            
            try {
                val output = when (event.type) {
                    PeerEventType.JOIN -> ListPeerOut(
                        peerCount = fullList.size,
                        newPeer = event.peerUuid,
                        listPeer = fullList
                    )
                    PeerEventType.LEAVE -> ListPeerOut(
                        peerCount = fullList.size,
                        removedPeer = event.peerUuid,
                        listPeer = fullList
                    )
                }
                conn.send(output)
            } catch (e: Exception) {
                println("Failed to broadcast peer event to ${key.userId}: ${e.message}")
            }
        }
    }

    override suspend fun unregisterPeerGroup(docId: Long, peerUuid: PeerUuid) {
        // 1. Leave the Redis group
        peerRedisSubscriber.leaveAndNotify(docId, peerUuid)

        // 2. Check if we should unsubscribe (no local connections left)
        // This is an optimization. 
        val remainingConnections = sessionManager.getConnections(docId)?.size ?: 0
        if (remainingConnections <= 1) { // 1 because the unregistering one might still be in the map briefly?
            // Actually sessionManager.unregister is called BEFORE or AFTER?
            // Typically unregisterPeerGroup is called from SessionManager or cleanup.
            // Let's assume safely: if map is empty.
            if (sessionManager.getConnections(docId).isNullOrEmpty()) {
                if (activeSubscriptions.remove(docId)) {
                    peerRedisSubscriber.unsubscribeFromPeerGroup(docId)
                }
            }
        }
    }

    override suspend fun close() {
        peerRedisSubscriber.close()
    }

}