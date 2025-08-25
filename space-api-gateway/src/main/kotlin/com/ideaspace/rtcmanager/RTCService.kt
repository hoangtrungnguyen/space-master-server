package com.ideaspace.rtcmanager

import com.ideaspace.core.kafkaMessage.*
import com.ideaspace.core.models.Process
import com.ideaspace.core.models.ProcessKey
import com.ideaspace.session.SessionManager
import io.ktor.server.websocket.*
import io.ktor.websocket.*
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable

@Serializable
data class RTCData(
    val peerCount: Int,
    val setOfPeer: Set<Process>,
    val removedPeer: Process? = null,
    val newPeer: Process? = null,
) {
    val type get(): String = "RTCData"
}

interface RTCManager {
    suspend fun publishPeer(process: Process)
    suspend fun removePeer(process: Process)
}

//TODO: Redis RTC Manager

class InMemoryRTCManagerImpl(
    val sessionManager: SessionManager
) : RTCManager {
    val rtcPool: MutableMap<Long, MutableSet<Process>> = HashMap()

    override suspend fun publishPeer(process: Process) {
        val docId = process.docId
        var setOfPeer = rtcPool[docId]
        if (setOfPeer != null) {
            setOfPeer.add(process)
            rtcPool[docId] = setOfPeer
        } else {
            val mutableSet = mutableSetOf<Process>(process)
            rtcPool[docId] = mutableSet
            setOfPeer = mutableSet
        }

        sessionManager.getConnections(docId)?.values?.let { connections ->
            coroutineScope {
                for (connection in connections) {
                    launch(connection.webSocket.coroutineContext) {
                        connection.send(
                            RTCData(
                                setOfPeer.size,
                                newPeer = process,
                                setOfPeer = setOfPeer,
                            )
                        )
                    }
                }
            }
        }
        println("rtcPool.values.size ${rtcPool.values.size}")
    }

    override suspend fun removePeer(process: Process) {
        val docId = process.docId
        val setOfPeer = rtcPool[docId]
        if (setOfPeer != null) {
            setOfPeer.remove(process)
            rtcPool[docId] = setOfPeer
            sessionManager.getConnections(docId)?.values?.let { connections ->
                coroutineScope {
                    for (connection in connections) {
                        launch(connection.webSocket.coroutineContext) {
                            connection.send<RTCData>(
                                RTCData(
                                    setOfPeer.size,
                                    removedPeer = process,
                                    setOfPeer = setOfPeer,
                                )
                            )
                        }
                    }
                }
            }
        } else {
            println("remove not found docId $docId")
        }
    }
}

class MessageFlowGateService() {

    private val isSyncFinishedState = MutableStateFlow(false)

    suspend fun transform(
        session: DefaultWebSocketServerSession, eventFlow: Flow<DocumentSyncEventValue>,
        onEditDocument: suspend (EditDocEventValue) -> Unit,
        onInitDocument: suspend (InitSyncEventValue) -> Unit,
    ) {

        val synEventFlow = eventFlow.filter { it is InitSyncEventValue || it is FinishSyncEventValue }
        val editEventFlow = eventFlow.filterIsInstance(EditDocEventValue::class)

        val validatedSyncFlow = createValidatedFlow(synEventFlow, session)

        val processableEditEvents = editEventFlow
            .combine(isSyncFinishedState) { event, isValid ->
                event to isValid
            }
            .filter { (_, isValid) -> isValid }
            .map { (event, _) -> event }


        val flow = merge<DocumentEvent>(validatedSyncFlow, processableEditEvents)
        flow.collect {
            when (it) {
                is EditDocEventValue -> onEditDocument(it)
                is InitSyncEventValue -> onInitDocument(it)
                is FinishSyncEventValue -> {
                    println("✅ Sync successful")
                }

                else -> {
                    println("Unhandled event $it")
                }
            }
        }
    }

    suspend fun close() {

    }


    private fun createValidatedFlow(
        synEventFlow: Flow<DocumentEvent>,
        session: DefaultWebSocketServerSession,
    ): Flow<DocumentEvent> {
        return synEventFlow.scan<DocumentEvent, ValidatedEvent>(
            ValidatedEvent(UnknownDocEvent(), false)
        ) { currentEventState, newEvent ->
            when (val currentState = currentEventState.event) {
                is InitSyncEventValue -> {
                    // currentState is init
                    // next Event can be both init or finish
                    // if next event is init -> client is trying to reset connection
                    if (newEvent is FinishSyncEventValue) {
                        ValidatedEvent(newEvent, true)
                    } else {
                        println("Sent Init Sync event again")
                        ValidatedEvent(newEvent, false)
                    }
                }

                is FinishSyncEventValue -> {
                    if (newEvent is FinishSyncEventValue) { // can not have 2 consecutive finish events
                        throw InvalidEventInSyncProcess("can not have 2 consecutive finish events")
                    } else {// if current state is finish and next event is init -> client is trying to re-sync
                        ValidatedEvent(newEvent, false)
                    }
                }

                is UnknownDocEvent -> {
                    if (newEvent is InitSyncEventValue) {
                        ValidatedEvent(newEvent, false)
                    } else {
                        throw InvalidEventInSyncProcess("First event for this connection must be init event")
                    }
                }

                else -> {
                    throw Exception("Unknown error")
                }
            }
        }.drop(0).catch {
            session.outgoing.send(Frame.Text("Error: ${it.message}"))
        }.onEach { (event, isValid) ->
            isSyncFinishedState.value = isValid
            if (!isValid) {
                println("❌ [Sync Processor] Sync is not finished")
                session.outgoing.send(Frame.Text("Error: Invalid event order for $event"))
            } else { // client notify its sync process is finish
                val finishEvent = event as FinishSyncEventValue
                val key = ProcessKey(
                    docId = finishEvent.docId,
                    userId = finishEvent.userId,
                    windowId = 123
                )
                println("Current Process Key: $key")
            }
        }.filter { it.event is InitSyncEventValue }.map {
            it.event
        }
    }

}

class InvalidEventInSyncProcess(message: String = "InvalidEvent") : Exception(message)

private data class ValidatedEvent(val event: DocumentEvent, val isValid: Boolean)
