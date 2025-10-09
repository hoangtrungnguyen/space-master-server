package com.space.subadmin.products

import jakarta.persistence.*

/**
 * Represents a Product entity.
 *
 * @param id The unique identifier for the product, automatically generated.
 * @param name The name of the product.
 * @param price The price of the product.
 */
@Table(name = "products")
@Entity
class Product(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long = 0,
    val name: String,
    val price: Double,
    val barcode: String? = "",
    val description: String? = "",
    val category: String? = ""
)
