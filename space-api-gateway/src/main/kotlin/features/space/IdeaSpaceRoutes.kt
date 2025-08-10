package com.space.features.space

import com.space.features.space.client.IdeaSpaceServerClient
import com.space.features.space.repository.WhiteboardRepository
import com.space.models.Subscription
import com.space.models.User
import com.space.services.KafkaProducerService
import com.space.services.dto.ServerOperation
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.route
import io.ktor.server.websocket.webSocket
import io.ktor.websocket.CloseReason
import io.ktor.websocket.close
import io.ktor.server.plugins.di.dependencies
import io.ktor.server.routing.application
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import kotlin.random.Random
import io.ktor.http.HttpStatusCode
import io.ktor.server.response.respondText


fun Route.whiteBoardRoutes(
) {
    route("/api") {
        route("/space") {

            post("/create") {
//                val whiteboardRepository: WhiteboardRepository =
//                    application.dependencies.resolve<WhiteboardRepository>()
//                println("Create whiteboard")

                val response = application.dependencies.resolve<IdeaSpaceServerClient>().createSpace()
                call.respond(status = HttpStatusCode.OK, response)
            }

            get("/all") {
                val whiteboardRepository: WhiteboardRepository =
                    application.dependencies.resolve<WhiteboardRepository>()
                val items = whiteboardRepository.findAll()
                call.respond(HttpStatusCode.OK, items)
            }

            get("/publish") {
                val producer = application.dependencies.resolve<KafkaProducerService>()
                producer.send(
                    "server-operation-topic",
                    ServerOperation(
                        operationId = "a1u2c3d4-e5f6-7890-g1h2-i3j4k5l6m7n8",
                        boardId = "board-98765",
                        userId = "user-12345",
                        revision = 1
                    )
                )

                call.respond(HttpStatusCode.OK)

            }
            get("/{id}") {
                val id = call.parameters["id"]!!
                val response = application.dependencies.resolve<IdeaSpaceServerClient>().getById(id)
                if (response == null) {
                    call.respond(HttpStatusCode.NotFound)
                    return@get
                }
                call.respond(status = HttpStatusCode.OK, response)
            }
        }
    }

    whiteBoardSocketRoutes()
}

fun Route.whiteBoardSocketRoutes() {

    route("/ws/whiteboard") {
        webSocket("/{boardId}") {
            val socketHandler = application.dependencies.resolve<IdeaSpaceSocketHandler>()

            val boardId = call.parameters["boardId"]

            if (boardId == null) {
                // If no boardId is provided, close the connection with an error
                close(CloseReason(CloseReason.Codes.VIOLATED_POLICY, "Board ID is required."))
                return@webSocket
            }

            // At this point, a WebSocket connection is established.
            // You would now delegate handling the session to your WhiteboardSocketHandler.
            // You would also need to get user details, likely from an authentication context.
            val user = User(
                userId = Random.nextInt().toString(),
                subscription = Subscription("Normal")
            )

            socketHandler.handle(this, boardId, user)
        }
    }
}


