package com.space.subadmin.customers

import org.springframework.data.jpa.repository.JpaRepository
import java.util.UUID


class CustomerAlreadyExistsException(message: String) : RuntimeException(message)

// --- JPA Repository ---

interface CustomerRepository : JpaRepository<Customer, Long> {
    /**
     * Finds a customer by their email address.
     */
    fun findByEmail(email: String): Customer?
}