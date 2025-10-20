package com.space.subadmin.customers

import com.space.subadmin.orders.Order
import jakarta.persistence.*
import org.hibernate.annotations.GenericGenerator
import java.time.Instant
import java.util.*

@Entity
@Table(name = "customers")
data class Customer(
    @Id
    @GenericGenerator(name = "tsid", strategy = "com.space.subadmin.config.TsidGenerator")
    @GeneratedValue(generator = "tsid")
    val id: Long = 0L,

    @Column(name = "uuid", nullable = false, unique = true)
    var uuid: UUID = UUID.randomUUID(),

    @Column(name = "first_name", nullable = false, length = 100)
    var firstName: String = "",

    @Column(name = "last_name", nullable = false, length = 100)
    var lastName: String = "",

    @Column(nullable = false, unique = true, length = 255)
    var email: String = "",

    @Column(name = "phone_number", length = 50)
    var phoneNumber: String? = null,

    @OneToMany(mappedBy = "customer", cascade = [CascadeType.ALL], fetch = FetchType.LAZY)
    var orders: MutableList<Order> = mutableListOf(),


    @Column(nullable = false, updatable = false)
    val createdAt: Instant = Instant.now(),

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