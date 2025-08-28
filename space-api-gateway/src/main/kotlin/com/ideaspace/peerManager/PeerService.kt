package com.ideaspace.peerManager

import com.ideaspace.core.dto.UUIDToString
import com.ideaspace.core.models.Process
import com.ideaspace.session.ListPeerOut
import com.ideaspace.session.SessionManager
import io.lettuce.core.RedisClient
import kotlinx.coroutines.CoroutineScope
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
    suspend fun unregisterPeerGroup(docId: Long, peerUuid: PeerUuid)
    suspend fun close()
}


class RedisPeerManagerImpl(
    private val sessionManager: SessionManager,
    private val redisClient: RedisClient,
    private val scope: CoroutineScope,
) : RTCPeerManager {

    private val peerRedisSubscriber = PeerRedisSubscriber(redisClient, scope)

    val peersInOthers: MutableMap<Long, MutableSet<PeerUuid>> = HashMap()

    val peersInThis: MutableMap<Long, MutableSet<PeerUuid>> = HashMap()

    init {

    }

    override suspend fun registerPeerGroup(docId: Long, peerUuid: PeerUuid) {
        if (peersInThis.containsKey(docId)) {
            peersInThis[docId]!!.add(peerUuid)
            val listPeer = mutableSetOf<UUID>()
            listPeer.addAll(peersInOthers[docId] ?: emptyList())
            listPeer.addAll(peersInThis[docId]!!)

            peerRedisSubscriber.publishListPeerEvent(
                docId = docId,
                listPeerOut = ListPeerOut(
                    listPeer.size,
                    listPeer = listPeer.toList(),
                    newPeer = peerUuid
                )
            )

        } else {
            peersInThis[docId] = mutableSetOf(
                peerUuid
            )

            peerRedisSubscriber.subscribeToPeerGroup(docId) { listPeerOut ->

                val allPeers = listPeerOut.listPeer.toMutableSet()

                val thisPeersNow = peersInThis[docId]!!

                val peersInOthersNow = allPeers.subtract(thisPeersNow)

                peersInOthers[docId] = peersInOthersNow.toMutableSet()

                val notConnectedPeer = thisPeersNow.subtract(allPeers)
                allPeers.addAll(notConnectedPeer)
                // send to all socket
                sessionManager.getConnections(docId)?.forEach { conn ->
                    conn.value.send(
                        ListPeerOut(
                            peerCount = allPeers.size,
                            listPeer = allPeers.toList(),
                        )
                    )
                }
            }


            val allPeer = mutableSetOf<UUID>()
            peerRedisSubscriber.getLastest(docId)?.listPeer?.let {
                allPeer.addAll(it)
            }
            allPeer.add(peerUuid)
            peerRedisSubscriber.publishListPeerEvent(
                docId = docId,
                listPeerOut = ListPeerOut(
                    allPeer.size,
                    listPeer = allPeer.toList(),
                    newPeer = peerUuid
                )
            )


        }
    }

    override suspend fun unregisterPeerGroup(docId: Long, peerUuid: PeerUuid) {
        peersInThis[docId]?.let {
            peersInThis[docId]!!.remove(peerUuid)

            if (peersInThis[docId]!!.isEmpty()) {

                peerRedisSubscriber.publishListPeerEvent(
                    docId,
                    ListPeerOut(
                        peersInOthers[docId]?.size ?: 0,
                        peerUuid,
                        listPeer = peersInOthers[docId]?.toList() ?: emptyList(),
                    )
                )
                peersInOthers.remove(docId)
                peersInThis.remove(docId)
                peerRedisSubscriber.unsubscribeFromPeerGroup(docId)
            } else {
                val allPeers = mutableListOf<UUID>()
                allPeers.addAll(peersInOthers[docId] ?: emptyList())
                allPeers.addAll(peersInThis[docId]!!)

                peerRedisSubscriber.publishListPeerEvent(
                    docId,
                    ListPeerOut(
                        allPeers.size,
                        peerUuid,
                        listPeer = allPeers.toList(),
                    )
                )
            }
        }
    }


    override suspend fun close() {
        peerRedisSubscriber.close()
    }

}