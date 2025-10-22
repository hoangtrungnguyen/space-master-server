package com.space.subadmin.products

import com.space.subadmin.db.ProductVariant
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository

@Repository
interface ProductVariantRepository : JpaRepository<ProductVariant, Long> {
    /**
     * Fetches all product variants with their associated product, brand, category, and creator user.
     * This is an efficient way to load all necessary data to build the DTOs,
     * avoiding N+1 query problems.
     */
    @Query("SELECT pv FROM ProductVariant pv JOIN FETCH pv.product p JOIN FETCH p.createdBy LEFT JOIN FETCH p.brand LEFT JOIN FETCH p.category")
    fun findAllWithDetails(): List<ProductVariant>
}