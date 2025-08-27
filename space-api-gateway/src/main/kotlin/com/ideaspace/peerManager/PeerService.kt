package com.ideaspace.peerManager

import com.ideaspace.core.dto.UUIDToString
import com.ideaspace.core.models.Process
import com.ideaspace.session.ListPeerOut
import com.ideaspace.session.SessionManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import java.util.*

@Serializable
data class PeerData(
    val process: Process,
    @Serializable(with = UUIDToString::class)
    val peerUuid: UUID
)

typealias PeerUuid = UUID

interface RTCPeerManager {
    suspend fun registerPeerGroup(docId: Long, peerUuid: PeerUuid)
}


class RedisPeerManagerImpl(
    private val sessionManager: SessionManager,
    private val peerRedisSubscriber: PeerRedisSubscriber,
    private val coroutineScope: CoroutineScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
) : RTCPeerManager {

    val peersInOthers: MutableMap<Long, MutableSet<PeerUuid>> = HashMap()

    val peersInThis: MutableMap<Long, MutableSet<PeerUuid>> = HashMap()

    init {

    }

    override suspend fun registerPeerGroup(docId: Long, peerUuid: PeerUuid) {
        if (peersInThis.containsKey(docId)) {
            peersInThis[docId]!!.add(peerUuid)
            val listPeer = mutableSetOf<UUID>()
            listPeer.addAll(peersInOthers[docId]!!)
            listPeer.addAll(peersInThis[docId]!!)

            peerRedisSubscriber.publishListPeerEvent(
                docId = docId,
                listPeerOut = ListPeerOut(
                    peersInOthers[docId]!!.size,
                    listPeer = listPeer.toList(),
                    newPeer = peerUuid
                )
            )

            // s
            peersInOthers[docId]!!.add(peerUuid)
        } else {
            peersInThis[docId] = mutableSetOf(
                peerUuid
            )

            coroutineScope.launch {
                peerRedisSubscriber.subscribeToPeerGroup(docId) { listPeerOut ->

                    val allPeers = listPeerOut.listPeer.toMutableSet()

                    val thisPeersNow = peersInThis[docId]!!

                    val peersInOthersNow = allPeers.subtract(thisPeersNow)

                    peersInOthers[docId] = peersInOthersNow.toMutableSet()

                    val notConnectedPeer = thisPeersNow.subtract(allPeers)
                    allPeers.addAll(notConnectedPeer)
                    // send to all socket
                    sessionManager.getConnections(docId)?.forEach { conn ->
                        conn.value.send(allPeers)
                    }
                }
            }


        }
    }

    suspend fun unregisterPeerGroup(docId: Long, peerUuid: PeerUuid) {
        peersInThis[docId]?.let {
            peersInThis[docId]!!.remove(peerUuid)

            if (peersInThis[docId]!!.isEmpty()) {

                peersInOthers.remove(docId)
                peerRedisSubscriber.unsubscribeFromPeerGroup(docId)
            } else {

            }
        }
    }


}