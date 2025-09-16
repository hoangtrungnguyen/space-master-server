package com.space.experiment.core

import com.space.experiment.domain.DocumentState
import com.space.experiment.domain.Nack
import com.space.experiment.domain.OperationLogEntry
import com.space.experiment.domain.OperationPayload
import com.space.experiment.services.OperationLog
import com.space.experiment.services.OperationQueue


/**
 * The main worker class that orchestrates the entire synchronization flow.
 * It dequeues operations and processes them according to the flowchart logic.
 */
class OperationProcessor(
    private val operationQueue: OperationQueue<OperationPayload>,
    private val operationLog: OperationLog,
    private val transformer: OperationalTransformer,
    private var documentState: DocumentState
) {

    val document get() = this.documentState

    /**
     * This method runs in a loop in a background worker process.
     * It continuously dequeues and processes operations.
     */
    fun processNextOperation(): OperationPayload? {
        // Step C: Dequeue Operation
        val operation = operationQueue.dequeue() ?: return null

        try {

            val serverRevision = documentState.currentRevision
            var opToApply = operation

            // Step D: Check if baseRevision matches server's currentRevision
            if (operation.revision != serverRevision) {
                // --- Conflict Resolution Path ---
                println("Conflict detected. Client rev: ${operation.revision}, Server rev: $serverRevision. Starting transformation.")

                // Step E: Fetch operations from log
                val conflictingOps = operationLog.getOpsSinceRevision(operation.revision)

                // Step F: Iteratively Transform
                val transformedOp = transformer.transform(operation, conflictingOps)

                // Step G: Check if transformation was successful
                if (transformedOp == null) {
                    // --- Rejection Path ---
                    handleTransformationFailure(operation)
                    return null
                }
                opToApply = transformedOp
                println("Transformation successful.")
            } else {
                println("No conflict. Applying operation directly.")
            }

            // --- Apply and Notify Path ---
            applyAndNotify(opToApply, documentState)
            return opToApply
        } catch (e: Exception) {
            // General error handling
            println("Error processing operation: ${e.message}")
            handleGenericFailure(operation, e.message ?: "An unknown error occurred.")

        }
        return null
    }

    private fun applyAndNotify(opToApply: OperationPayload, currentState: DocumentState) {

        val newState = currentState.applyOp(opToApply)

        val newRevision = currentState.currentRevision + 1
        currentState.currentRevision = newRevision // I: Increment revision

        updateStateAndLogOperation(newState, opToApply)
        println("Successfully applied op. New revision is $newRevision.")

//        notifier.broadcastToOthers(opToApply.clientId, opToApply.documentId, opToApply, newRevision)

        // L: Send ACK to original client
//        val ack = Ack(success = true, newRevision = newRevision, transformedOp = opToApply)
//        notifier.sendAck(opToApply.clientId, ack)
    }

    private fun updateStateAndLogOperation(
        newState: DocumentState,
        opToApply: OperationPayload
    ) {
        operationLog.saveLogEntry(
            OperationLogEntry(
                revision = newState.currentRevision,
                operation = opToApply
            )
        )
    }

    private fun handleTransformationFailure(operation: OperationPayload) {
        // Corresponds to steps M and N
//        println("Transformation failed for client ${operation.processId}. Sending NACK.")
        val nack = Nack(reason = "Unresolvable conflict. Please re-fetch the document.")
//        notifier.sendNack(operation.processId, nack)
//         N: Log failed operation for debugging
//        logFailedOperation(operation, "Transformation failure")
    }

    private fun handleGenericFailure(operation: OperationPayload, reason: String) {
//        val nack = Nack(reason = reason)
//        notifier.sendNack(operation.clientId, nack)
//        logFailedOperation(operation, reason)
    }

    private fun logFailedOperation(operation: OperationPayload, reason: String) {
//         Placeholder for structured logging (e.g., to a file, ElasticSearch, etc.)
//        System.err.println("FAILED_OPERATION: client=${operation.clientId}, doc=${operation.documentId}, reason=$reason, op=$operation")
    }
}
