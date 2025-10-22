package com.space.subadmin.products

import com.space.subadmin.db.Product
import com.space.subadmin.db.ProductVariant
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import java.util.*

interface ProductRepository : JpaRepository<Product, Long> {

    @Query(
        """
        SELECT p
        FROM Product p
        LEFT JOIN FETCH p.brand
        LEFT JOIN FETCH p.category
        LEFT JOIN FETCH p.variants
        """
    )
    fun findAllWithDetails(): List<Product>

    /**
     * Finds a product by its ID, eagerly fetching all associations needed for the detail page.
     */
    @Query(
        """
        SELECT p FROM Product p
        LEFT JOIN FETCH p.brand
        LEFT JOIN FETCH p.category
        LEFT JOIN FETCH p.createdBy
        LEFT JOIN FETCH p.variants
        WHERE p.id = :id
        """
    )
    fun findProductDetailById(id: Long): Optional<Product>
}

interface ProductVariantsRepository : JpaRepository<ProductVariant, Long> {
}
