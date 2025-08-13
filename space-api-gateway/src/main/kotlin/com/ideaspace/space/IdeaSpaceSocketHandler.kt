package com.ideaspace.space

import com.ideaspace.core.models.User
import com.ideaspace.session.SessionManager
import com.ideaspace.session.WhiteboardConnection
import com.ideaspace.space.handlers.OperationHandler
import com.ideaspace.space.models.Operation
import com.ideaspace.space.models.OperationDto
import com.ideaspace.space.models.OperationType
import com.ideaspace.space.service.IdeaSpaceSessionService
import io.ktor.websocket.*
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.serialization.json.Json


/**
 * Orchestrates the entire lifecycle of a user's WebSocket connection for a whiteboard.
 *
 * This class is the heart of the real-time whiteboard feature. It is responsible for:
 * 1.  Enforcing business rules (e.g., subscription limits).
 * 2.  Managing user session registration.
 * 3.  Coordinating with other services to fetch initial state.
 * 4.  Listening for incoming real-time operations and delegating them to the correct handlers.
 * 5.  Ensuring clean-up on user disconnection.
 *
 * @param sessionManager The service that tracks active connections for each board on this server instance.
 * @param sessionService The service that ensures a board session is active and fetches its initial state.
 * @param userCountRepository The repository acting as the single source of truth for user counts.
 * @param kafkaProducer The service used to publish events for decoupled processing.
 * @param handlers A map that provides O(1) lookup to delegate an operation to its specific handler.
 */
class IdeaSpaceSocketHandler(
    private val sessionManager: SessionManager,
    private val sessionService: IdeaSpaceSessionService,
//    private val userCountRepository: UserCountRepository,
//    private val kafkaProducer: KafkaProducerService,
    private val handlers: Map<OperationType, OperationHandler>
) {

    /**
     * Handles a new WebSocket connection for a given user and board.
     */
    suspend fun handle(session: DefaultWebSocketSession, boardId: String, user: User) {
        // 1. Enforce Business Rules (Subscription Limit)
        // This check is performed first to ensure 100% accuracy for the business rule.

//        if (userCountRepository.getCount(boardId) >= user.subscription.maxUsers) {
//            session.close(CloseReason(CloseReason.Codes.VIOLATED_POLICY, "User limit for this board has been reached."))
//            return
//        }

        // Create the connection object early to use in logging and registration.
        val connection = WhiteboardConnection(userId = user.id, session = session)

        // The main logic is wrapped in a try/finally block to guarantee cleanup.
        try {
            // 2. Update State and Register Session
//            val newCount = userCountRepository.incrementAndGet(boardId)
            sessionManager.register(connection, boardId)

            // 3. Publish Event for Non-Critical Updates (e.g., client UI)
            // This is decoupled via Kafka. Other services can listen to this event.
//            kafkaProducer.publish("user-count-changed", mapOf("boardId" to boardId, "count" to newCount))

            // 4. Fetch and Send Initial Board State
            // Ensures the new user gets the full current state of the whiteboard immediately.
            val initialState = sessionService.ensureActiveSessionAndGetState(boardId)
            connection.send(initialState)

            // 5. Main Loop: Listen for and Delegate Incoming Operations
            // The server now listens for real-time operations from this specific client.
            session.incoming
                .receiveAsFlow() // Convert incoming channel to a Flow
                .mapNotNull { frame ->
                    // Process only text frames and ignore others.
                    if (frame is Frame.Text) {
                        try {
                            println("Data: ${frame.readText()}")
                            Json.decodeFromString<OperationDto>(frame.readText())
                        } catch (e: Exception) {
                            println("Failed to deserialize operation for user ${user.id}: ${e.message}")
                            e.printStackTrace()
                            session.outgoing.send(Frame.Text("FAILURE"))
                            null // Ignore malformed frames.
                        }
                    } else null
                }
                .collect { operation ->
                    // O(1) lookup to find the correct handler for the operation's type.
                    val operation = when (operation.type) {
                        OperationType.Document -> Operation.DocumentOperation(
                            position = operation.position,
                        )

                        OperationType.Table -> TODO()
                        OperationType.Shape -> TODO()
                    }
                    handlers[operation.type]?.handle(connection, operation)
                        ?: println("No handler found for operation type: ${operation.type}")
                }

        } catch (e: Exception) {
            println("An error occurred in the session for user ${user.id} on board $boardId: ${e.message}")
        } finally {
            // 6. Guaranteed Cleanup on Disconnection
            // This block executes whether the connection closes normally, with an error, or on timeout.
            sessionManager.unregister(connection, boardId)
//            val finalCount = userCountRepository.decrementAndGet(boardId)
//            kafkaProducer.publish("user-count-changed", mapOf("boardId" to boardId, "count" to finalCount))
            println("Cleanup complete for user ${user.id} on board $boardId.")
        }
    }
}
