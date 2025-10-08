package com.space.subadmin.products

import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id

/**
 * Represents a Product entity.
 *
 * @param id The unique identifier for the product, automatically generated.
 * @param name The name of the product.
 * @param price The price of the product.
 */
@Entity
class Product(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,
    val name: String,
    val price: Double
)
