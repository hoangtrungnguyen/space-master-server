package com.space.features.space.handlers

import com.space.features.space.models.Operation
import com.space.session.WhiteboardConnection

open class OperationHandler {
    suspend fun handle(connection: WhiteboardConnection, operation: Operation) {
        println("Handle ${operation}")
        connection.send("Handling")
    }
}