package com.space.com.ideaspace.space.handlers

import com.space.com.ideaspace.session.WhiteboardConnection
import com.space.com.ideaspace.space.models.Operation

open class OperationHandler {
    suspend fun handle(connection: WhiteboardConnection, operation: Operation) {
        println("Handle ${operation}")
        connection.send("Handling")
    }
}