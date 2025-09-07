package com.space

import com.space.transform.*
import io.ktor.server.application.*
import io.ktor.server.netty.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.polymorphic
import kotlinx.serialization.modules.subclass


// --- Ktor Server Setup ---

fun main(args: Array<String>) {
    EngineMain.main(args)
}

fun Application.module() {
    // Install the ContentNegotiation plugin to handle JSON.
    install(ContentNegotiation) {
        // Configure json to handle our polymorphic ClientOperation class
        SerializersModule {
            polymorphic(ClientOperation::class) {
                subclass(InsertOperation::class)
                subclass(DeleteOperation::class)
            }
        }
    }

    // Create a single handler instance to be used for all requests.
    val operationHandler = OperationHandler()

    // Define the routing for the application.
    routing {
        post("/operation") {
            // Receive the JSON payload and deserialize it into a ClientOperation object.
            val clientOp = call.receive<ClientOperation>()

            // Process the operation.
            val operationsToApply = operationHandler.handleOperation(clientOp)

            // Send the resulting list of operations back to the client as JSON.
            call.respond(operationsToApply)
        }

        get("/state") {
            // A simple endpoint to check the current state of the document on the server.
            call.respondText("Version: ${DocumentStore.getHistory().size}, Document: '${DocumentStore.getDocumentState()}'")
        }

        get("/") {
            call.respondText("Home")
        }
    }
}
