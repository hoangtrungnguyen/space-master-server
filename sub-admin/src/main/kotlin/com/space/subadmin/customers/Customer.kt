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

    @Column(name = "created_at", nullable = false, updatable = false, columnDefinition = "TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP")
    val createdAt: Instant? = null,

    @Column(name = "updated_at", nullable = false, columnDefinition = "TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP")
    var updatedAt: Instant? = null
) {
    val fullName: String
        get() = "$firstName $lastName"
}