package com.space.subadmin.products

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query

interface ProductRepository : JpaRepository<Product, Long> {
    @Query("SELECT p FROM Product p LEFT JOIN FETCH p.brand LEFT JOIN FETCH p.category LEFT JOIN FETCH p.variants")
    fun findAllWithDetails(): List<Product>
}

interface ProductVariantsRepository : JpaRepository<ProductVariant, Long> {
}