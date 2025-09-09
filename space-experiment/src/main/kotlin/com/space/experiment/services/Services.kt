package com.space.experiment.services

import com.space.experiment.domain.*

/**
 * Interface for a persistent queue (e.g., Kafka, Redis Streams)
 * This corresponds to block B in the flowchart.
 */
interface OperationQueue<T> {
    fun enqueue(operation: T)
    fun dequeue(): T?
}

/**
 * Interface for interacting with the primary document data store (e.g., Postgres table).
 */
interface DocumentStore {
    suspend fun getDocumentState(documentId: Long): DocumentState?

    /**
     * Atomically updates the document state, increments the revision, and logs the operation.
     * This corresponds to the "Atomic DB Transaction" block (H, I, J) in the flowchart.
     */
    fun updateStateAndLogOperation(documentState: DocumentState, operation: OperationPayload)
}

/**
 * Interface for retrieving historical operations (e.g., from a Postgres log table).
 * This corresponds to block E in the flowchart.
 */
interface OperationLog {
    fun getOpsSinceRevision(revision: Long): List<OperationLogEntry>
    fun saveLogEntry(logEntry: OperationLogEntry)
}

/**
 * Interface for sending notifications back to clients via WebSockets.
 * This corresponds to blocks K, L, and M in the flowchart.
 */
interface WebSocketNotifier {
    /**
     * Broadcasts the applied operation to all clients except the original sender.
     */
    fun broadcastToOthers(
        originalClientId: String,
        documentUuid: String,
        operation: EditTextOperation,
        newRevision: Long
    )

    /**
     * Sends a success acknowledgement to the original client.
     */
//    fun sendAck(clientId: String, ack: AckTransformedOperation)

    /**
     * Sends a failure negative acknowledgement to the original client.
     */
    fun sendNack(clientId: String, nack: Nack)
}
