package com.space.experiment

import com.space.experiment.core.OperationProcessor
import com.space.experiment.core.OperationalTransformer
import com.space.experiment.domain.*
import com.space.experiment.services.DocumentHandlerImpl
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.netty.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.plugins.di.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.polymorphic
import kotlinx.serialization.modules.subclass

fun main(args: Array<String>) {
    EngineMain.main(args)
}

fun Application.module() {
    install(ContentNegotiation) {
        json()
        SerializersModule {

            polymorphic(OperationPayload::class) {
                subclass(InsertText::class)
                subclass(DeleteText::class)
            }
        }
    }


    val documentHandler = DocumentHandlerImpl()
    val processQueue = SimpleOperationQueue()

    dependencies.provide {
        OperationProcessor(
            operationQueue = processQueue,
            operationLog = documentHandler,
            transformer = OperationalTransformer(),
            documentState = DocumentState(
                documentId = 1,
                1L,
                ""
            )
        )
    }


    routing {
        post("/operation") {
            val operation = processQueue.enqueue(call.receive<OperationPayload>())
            val applyAndNotify = dependencies.resolve<OperationProcessor>().processNextOperation()
            if (applyAndNotify != null) {
                call.respond(applyAndNotify)
            } else {
                call.respondText(text = "NOTHING is applied", status = HttpStatusCode.Forbidden)
            }
        }

        get("/state") {
            val data = dependencies.resolve<OperationProcessor>().document
            call.respond(data)
        }

        get("/") {
            call.respondText("Home")
        }
    }
}