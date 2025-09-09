package com.space.experiment.domain

import com.space.experiment.services.OperationQueue

//class ProcessQueueImpl : OperationQueue {
//
//    override fun enqueue(operation: EditTextOperation) {
//        TODO("Not yet implemented")
//    }
//
//    override fun dequeue(): EditTextOperation? {
//        TODO("Not yet implemented")
//    }
//}

class SimpleOperationQueue : OperationQueue<OperationPayload> {
    val pendingChanges = ArrayDeque<OperationPayload>()
    override fun enqueue(operation: OperationPayload) {
        pendingChanges.add(operation)
    }

    override fun dequeue(): OperationPayload? {
        return pendingChanges.removeFirstOrNull()
    }

}