package com.space.transform

import kotlinx.serialization.Serializable


sealed class C

interface OperationPayload


@Serializable
data class InsertText(
    val text: String,
    val position: Int
) : OperationPayload

enum class OperationStatus {
    UNKNOWN,
    PENDING,
    APPLIED
}

interface Operation {
    val revision: Int
    val payload: OperationPayload
    val status: OperationStatus
        get() = TODO()
}

@Serializable
data class AppliedOperation(
    val client: String,
    override val revision: Int,
    override val payload: OperationPayload
) : Operation {
    override val status: OperationStatus
        get() = OperationStatus.APPLIED
}


@Serializable
data class PendingOperation(
    val client: String,
    override val revision: Int,
    override val payload: OperationPayload
) : Operation {
    override val status: OperationStatus
        get() = OperationStatus.PENDING
}

// --- In-Memory Server State ---

/**
 * Singleton object to act as our in-memory data store.
 * It holds the current state of the document and the history of all applied operations.
 */
object DocumentStore {
    // The document is represented as a mutable list of characters.
    val document = mutableListOf('H', 'e', 'l', 'l', 'o')

    // A log of all operations successfully applied to the document.
    val revisionLog = mutableListOf<AppliedOperation>()

    val pendingChanges = mutableListOf<PendingOperation>()

    @Synchronized
    fun getDocumentState(): String = document.joinToString("")

    @Synchronized
    fun getHistory(): List<AppliedOperation> = ArrayList(revisionLog)

    @Synchronized
    fun addToHistory(op: AppliedOperation) = revisionLog.add(op)

    @Synchronized
    fun addToPending(op: PendingOperation) = pendingChanges.add(op)

    @Synchronized
    fun applyToDocument(op: AppliedOperation) {
        val payload = op.payload
        when (payload) {
            is InsertText -> {
                if (payload.position >= 0 && payload.position <= document.size) {
                    document.add(payload.position, payload.char)
                }
            }

            is DeleteOperation -> {
                if (op.position >= 0 && op.position < document.size) {
                    document.removeAt(op.position)
                }
            }
        }
    }
}

// --- Core Synchronization Logic ---

/**
 * Handles the Operational Transformation (OT) logic for processing client operations.
 * This class is now thread-safe for use in a server environment.
 */
class OperationHandler {
    @Synchronized
    fun handleOperation(operation: ClientOperation): List<ClientOperation> {
        val currentHistory = DocumentStore.getHistory()
        println("---")
        println("Received from ${operation.sourceId}: $operation")
        println("Current Server State: '${DocumentStore.getDocumentState()}' (Version: ${currentHistory.size})")

        val concurrentOps = currentHistory.drop(operation.clientVersion)

        var transformedOp = operation
        for (serverOpContainer in concurrentOps) {
            transformedOp = transform(transformedOp, serverOpContainer.operation)
        }
        println("Transformed Operation: $transformedOp")

        DocumentStore.applyToDocument(transformedOp)

        val newServerVersion = currentHistory.size + 1
        val newServerOperation = ServerOperation(transformedOp, newServerVersion)
        DocumentStore.addToHistory(newServerOperation)

        println("New Server State: '${DocumentStore.getDocumentState()}' (Version: ${newServerOperation.serverVersion})")

        return concurrentOps.map { it.operation } + newServerOperation.operation
    }

    private fun transform(opA: ClientOperation, opB: ClientOperation): ClientOperation {
        return when (opA) {
            is InsertOperation -> when (opB) {
                is InsertOperation -> {
                    if (opB.position <= opA.position) opA.copy(position = opA.position + 1)
                    else opA
                }

                is DeleteOperation -> {
                    if (opB.position < opA.position) opA.copy(position = opA.position - 1)
                    else opA
                }
            }

            is DeleteOperation -> when (opB) {
                is InsertOperation -> {
                    if (opB.position < opA.position) opA.copy(position = opA.position + 1)
                    else opA
                }

                is DeleteOperation -> {
                    when {
                        opB.position < opA.position -> opA.copy(position = opA.position - 1)
                        opB.position == opA.position -> opA.copy(position = -1) // No-op
                        else -> opA
                    }
                }
            }
        }
    }
}