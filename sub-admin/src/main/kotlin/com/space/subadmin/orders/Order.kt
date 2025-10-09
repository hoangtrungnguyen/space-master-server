package com.space.subadmin.orders


import jakarta.persistence.*

@Table(name = "orders")
@Entity
class Order(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,
    val orderTime: Long,
    val finalBill: Double,
    val status: OrderStatus
)


enum class OrderStatus {
    PENDING,
    CANCELED,
    SUCCESS,
    FAILURE
}

