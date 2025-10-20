package com.space.subadmin.customers

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.bind.annotation.*
import java.time.Instant
import java.util.UUID

// --- Data Transfer Objects (DTOs) ---

/**
 * DTO for the client request to create a new customer.
 * Updated to match the new Customer entity structure.
 */
data class CustomerRequest(
    val firstName: String,
    val lastName: String,
    val email: String,
    val phoneNumber: String? = null
)

/**
 * DTO for the server response after creating a customer.
 * Updated to include fullName.
 */
data class CustomerResponse(
    val uuid: UUID,
    val fullName: String,
    val email: String,
    val phoneNumber: String?,
    val createdAt: Instant
)



// --- Service Layer ---



// --- API Controller ---

@RestController
@RequestMapping("/api/v1/customers")
class CustomerController(private val customerService: CustomerService) {

    @PostMapping
    fun createCustomer(@RequestBody request: CustomerRequest): ResponseEntity<CustomerResponse> {
        val customer = customerService.createCustomer(request)

        // Map the updated Customer entity to the CustomerResponse DTO.
        val response = CustomerResponse(
            uuid = customer.uuid,
            fullName = customer.fullName, // Use the new computed property
            email = customer.email,
            phoneNumber = customer.phoneNumber, // Use the renamed field
            createdAt = customer.createdAt
        )

        // On successful creation, return 201 Created.
        return ResponseEntity.status(HttpStatus.CREATED).body(response)
    }

    /**
     * Handles the specific case where a customer with the given email already exists,
     * returning a 409 Conflict status code.
     */
    @ExceptionHandler(CustomerAlreadyExistsException::class)
    fun handleCustomerExists(ex: CustomerAlreadyExistsException): ResponseEntity<Map<String, String>> {
        return ResponseEntity
            .status(HttpStatus.CONFLICT)
            .body(mapOf("error" to ex.message!!))
    }
}
