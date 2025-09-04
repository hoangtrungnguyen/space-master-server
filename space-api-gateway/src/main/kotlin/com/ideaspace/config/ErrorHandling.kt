package com.ideaspace.config

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.plugins.statuspages.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import kotlinx.serialization.SerializationException
import kotlinx.serialization.Serializable

fun Application.configureErrorHandling() {
    install(StatusPages) {
        exception<SerializationException> { call, cause ->
            printDetailedError("SERIALIZATION ERROR", cause, call)
            call.respond(
                HttpStatusCode.BadRequest,
                ErrorResponse(
                    code = "INVALID_JSON_FORMAT",
                    message = "Unable to parse request body",
                    details = cause.message,
                    timestamp = System.currentTimeMillis()
                )
            )
        }
        
        exception<IllegalArgumentException> { call, cause ->
            printDetailedError("ILLEGAL ARGUMENT ERROR", cause, call)
            call.respond(
                HttpStatusCode.BadRequest,
                ErrorResponse(
                    code = "INVALID_REQUEST_DATA",
                    message = "Invalid argument provided",
                    details = cause.message,
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
                            code = "CONTENT_TRANSFORMATION_FAILED",
                            message = "Unable to process request content",
                            details = cause.message,
                            timestamp = System.currentTimeMillis()
                        )
                    )
                }
                else -> {
                    printDetailedError("UNHANDLED EXCEPTION", cause, call)
                    call.respond(
                        HttpStatusCode.InternalServerError,
                        ErrorResponse(
                            code = "INTERNAL_SERVER_ERROR",
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
    val code: String,
    val message: String,
    val details: String? = null,
    val timestamp: Long
)
