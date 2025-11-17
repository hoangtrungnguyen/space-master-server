package com.space.subadmin.customers

import com.space.subadmin.db.Customer
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import java.util.UUID


class CustomerAlreadyExistsException(message: String) : RuntimeException(message)

// --- JPA Repository ---

interface CustomerRepository : JpaRepository<Customer, Long> {
    /**
     * Finds a customer by their email address.
     */
    fun findByEmail(email: String): Customer?

    @Query("SELECT c FROM Customer c WHERE lower(concat(c.firstName, ' ', c.lastName)) = lower(:name)")
    fun findByName(@Param("name") name: String): List<Customer>

    fun countByCreatedAtAfter(date: java.time.Instant): Long
}