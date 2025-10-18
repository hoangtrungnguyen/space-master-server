package com.space.subadmin.customers

import com.space.subadmin.orders.Order
import jakarta.persistence.*
import java.time.Instant
import java.util.*

@Entity
@Table(name = "customers")
class Customer(
    @Id
    val id: UUID = UUID.randomUUID(),

    @Column(name = "first_name", nullable = false, length = 100)
    val firstName: String = "",

    @Column(name = "last_name", nullable = false, length = 100)
    val lastName: String = "",

    @Column(nullable = false, unique = true, length = 255)
    val email: String = "",

    @Column(name = "phone_number", length = 50)
    val phoneNumber: String? = null,

    @OneToMany(mappedBy = "customer", cascade = [CascadeType.ALL], fetch = FetchType.LAZY)
    val orders: List<Order> = listOf(),


    @Column(nullable = false, updatable = false)
    var createdAt: Instant = Instant.now(),

    @Column(nullable = false)
    var updatedAt: Instant = Instant.now()
) {
    val fullName: String
        get() = "$firstName $lastName"

    @PreUpdate
    fun onUpdate() {
        updatedAt = Instant.now()
    }
}