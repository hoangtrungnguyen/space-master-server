package com.ideaspace.rtcmanager

import com.ideaspace.core.kafkaMessage.*
import com.ideaspace.core.models.ProcessKey
import io.ktor.server.websocket.*
import io.ktor.websocket.*
import kotlinx.coroutines.flow.*
import java.util.concurrent.ConcurrentHashMap


data class RTCData(
    val port: Int,
    val url: String
) {
}


class RTCManager() {

    private val rtcPool: ConcurrentHashMap<Long, MutableSet<RTCData>> = ConcurrentHashMap()

    fun add(docId: Long, rtcData: RTCData) {
        val setOfPeer = rtcPool[docId]
        if (setOfPeer != null) {
            setOfPeer.add(rtcData)
            rtcPool[docId] = setOfPeer
        } else {
            val mutableSet = mutableSetOf<RTCData>(rtcData)
            rtcPool[docId] = mutableSet
        }

        println("rtcPool.values.size ${rtcPool.values.size}")
    }

    fun remove(docId: Long, url: String) {
        val setOfPeer = rtcPool[docId]
        if (setOfPeer != null) {
            setOfPeer.removeIf { it.url == url }
            rtcPool[docId] = setOfPeer
        } else {
            println("remove not found docId $docId")
        }
    }
}

class RTCService() {

    private val isSyncFinishedState = MutableStateFlow(false)

    private val changeFlow: MutableSharedFlow<RTCData> = MutableSharedFlow()

    private val rtcManager: ConcurrentHashMap<Long, ConcurrentHashMap<ProcessKey, RTCData>> = ConcurrentHashMap()

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
//                rtcManager.compute(finishEvent.docId){_, oldValue ->
//                    if(oldValue == null){
//                        ConcurrentHashMap().let {
//                            it.compute (
//                                key, RTCData(
//
//                                )
//                            )
//                        }
//                    } else {
//                       oldValue.let {
//
//                       }
//                    }
//                }
            }
        }.filter { it.event is InitSyncEventValue }.map {
            it.event
        }
    }

}

class InvalidEventInSyncProcess(message: String = "InvalidEvent") : Exception(message)

private data class ValidatedEvent(val event: DocumentEvent, val isValid: Boolean)
