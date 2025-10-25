package com.space.subadmin.exception

import com.fasterxml.jackson.databind.ObjectMapper
import jakarta.servlet.http.HttpServletRequest
import org.springframework.core.Ordered
import org.springframework.core.annotation.Order
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.http.converter.HttpMessageNotReadableException
import org.springframework.web.HttpMediaTypeNotSupportedException
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import org.springframework.web.servlet.NoHandlerFoundException
import org.springframework.web.servlet.resource.NoResourceFoundException

/**
 * Global exception handler for API endpoints to ensure consistent JSON error responses.
 * This advice is ordered to have high precedence, ensuring it catches exceptions before
 * default Spring Boot handlers.
 *
 * @param objectMapper Used for serializing error details to JSON.
 */
@RestControllerAdvice(basePackages = ["com.space.subadmin"]) // Apply to controllers in this package
@Order(Ordered.HIGHEST_PRECEDENCE) // Ensure this advice is processed first
class ApiExceptionHandler(private val objectMapper: ObjectMapper) {

    /**
     * Handles cases where the client sends an unsupported Content-Type header.
     * Returns a 415 Unsupported Media Type response.
     */
    @ExceptionHandler(HttpMediaTypeNotSupportedException::class)
    fun handleHttpMediaTypeNotSupportedException(
        ex: HttpMediaTypeNotSupportedException,
        request: HttpServletRequest
    ): ResponseEntity<Map<String, Any>> {
        val errorDetails = mapOf(
            "timestamp" to System.currentTimeMillis(),
            "status" to HttpStatus.UNSUPPORTED_MEDIA_TYPE.value(),
            "error" to "Unsupported Media Type",
            "message" to ex.message.orEmpty(),
            "path" to request.requestURI
        )
        return ResponseEntity(errorDetails, HttpStatus.UNSUPPORTED_MEDIA_TYPE)
    }

    /**
     * Handles cases where the request body is malformed JSON or cannot be read
     * (e.g., missing required fields, invalid data types).
     * Returns a 400 Bad Request response.
     */
    @ExceptionHandler(HttpMessageNotReadableException::class)
    fun handleHttpMessageNotReadableException(
        ex: HttpMessageNotReadableException,
        request: HttpServletRequest
    ): ResponseEntity<Map<String, Any>> {
        val errorDetails = mapOf(
            "timestamp" to System.currentTimeMillis(),
            "status" to HttpStatus.BAD_REQUEST.value(),
            "error" to "Bad Request",
            "message" to "Malformed JSON request body or unreadable message: ${ex.message.orEmpty()}",
            "path" to request.requestURI
        )
        return ResponseEntity(errorDetails, HttpStatus.BAD_REQUEST)
    }

    /**
     * Handles validation errors that occur when using `@Valid` or `@Validated` on `@RequestBody`
     * or `@ModelAttribute` parameters.
     * Returns a 400 Bad Request response with details about the validation failures.
     */
    @ExceptionHandler(MethodArgumentNotValidException::class)
    fun handleMethodArgumentNotValidException(
        ex: MethodArgumentNotValidException,
        request: HttpServletRequest
    ): ResponseEntity<Map<String, Any>> {
        val errors = ex.bindingResult.fieldErrors.map { error ->
            mapOf("field" to error.field, "message" to error.defaultMessage.orEmpty())
        }
        val errorDetails = mapOf(
            "timestamp" to System.currentTimeMillis(),
            "status" to HttpStatus.BAD_REQUEST.value(),
            "error" to "Validation Error",
            "message" to "Request validation failed",
            "details" to errors,
            "path" to request.requestURI
        )
        return ResponseEntity(errorDetails, HttpStatus.BAD_REQUEST)
    }

    /**
     * Generic exception handler for any other unhandled exceptions.
     * Returns a 500 Internal Server Error response.
     */
    @ExceptionHandler(Exception::class)
    fun handleAllExceptions(
        ex: Exception,
        request: HttpServletRequest
    ): ResponseEntity<Map<String, Any>> {
        val errorDetails = mapOf(
            "timestamp" to System.currentTimeMillis(),
            "status" to HttpStatus.INTERNAL_SERVER_ERROR.value(),
            "error" to "Internal Server Error",
            "message" to "An unexpected error occurred: ${ex.message.orEmpty()}",
            "path" to request.requestURI
        )
        return ResponseEntity(errorDetails, HttpStatus.INTERNAL_SERVER_ERROR)
    }
}