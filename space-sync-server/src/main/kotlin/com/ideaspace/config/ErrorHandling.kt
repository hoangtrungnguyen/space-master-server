package com.ideaspace.config

import io.ktor.http.HttpStatusCode
import io.ktor.server.application.Application
import io.ktor.server.application.ApplicationCall
import io.ktor.server.application.install
import io.ktor.server.plugins.statuspages.StatusPages
import io.ktor.server.request.contentType
import io.ktor.server.request.httpMethod
import io.ktor.server.request.uri
import io.ktor.server.response.respond
import kotlinx.serialization.Serializable
import kotlinx.serialization.SerializationException
import kotlin.collections.component1
import kotlin.collections.component2

fun Application.configureErrorHandling() {
    install(StatusPages) {
        exception<SerializationException> { call, cause ->
            printDetailedError("SERIALIZATION ERROR", cause, call)
            call.respond(
                HttpStatusCode.BadRequest,
                ErrorResponse(
                    error = "Invalid JSON format",
                    message = cause.message ?: "Unable to parse request body",
                    details = "Please check your JSON syntax and ensure all required fields are present",
                    timestamp = System.currentTimeMillis()
                )
            )
        }

        exception<IllegalArgumentException> { call, cause ->
            printDetailedError("ILLEGAL ARGUMENT ERROR", cause, call)
            call.respond(
                HttpStatusCode.BadRequest,
                ErrorResponse(
                    error = "Invalid request data",
                    message = cause.message ?: "Invalid argument provided",
                    details = null,
                    timestamp = System.currentTimeMillis()
                )
            )
        }

        exception<Exception> { call, cause ->
            when (cause::class.simpleName) {
                "ContentTransformationException" -> {
                    printDetailedError("CONTENT TRANSFORMATION ERROR", cause, call)
                    call.respond(
                        HttpStatusCode.BadRequest,
                        ErrorResponse(
                            error = "Content transformation failed",
                            message = cause.message ?: "Unable to process request content",
                            details = "Check Content-Type header and request body format",
                            timestamp = System.currentTimeMillis()
                        )
                    )
                }
                else -> {
                    printDetailedError("UNHANDLED EXCEPTION", cause, call)
                    call.respond(
                        HttpStatusCode.InternalServerError,
                        ErrorResponse(
                            error = "Internal server error",
                            message = "An unexpected error occurred",
                            details = "${cause::class.simpleName}: ${cause.message ?: "No additional details available"}",
                            timestamp = System.currentTimeMillis()
                        )
                    )
                }
            }
        }


    }
}

private fun printDetailedError(errorType: String, cause: Throwable, call: ApplicationCall) {
    val separator = "=".repeat(80)
    println(separator)
    println("!!! $errorType !!!")
    println(separator)

    // Request details
    println("REQUEST DETAILS:")
    println("  Path: ${call.request.uri}")
    println("  Method: ${call.request.httpMethod.value}")
    println("  Content-Type: ${call.request.contentType()}")
    println("  Headers:")
    call.request.headers.entries().forEach { (name, values) ->
        println("    $name: ${values.joinToString(", ")}")
    }
    println()

    // Error details
    println("ERROR DETAILS:")
    println("  Type: ${cause::class.qualifiedName}")
    println("  Message: ${cause.message}")
    println("  Cause: ${cause.cause?.message ?: "None"}")
    println()

    // Stack trace
    println("STACK TRACE:")
    println(cause.stackTraceToString())

    println(separator)
    println()
}

@Serializable
data class ErrorResponse(
    val error: String,
    val message: String,
    val details: String? = null,
    val timestamp: Long
)
