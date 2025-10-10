package com.space.subadmin.products

import org.springframework.data.jpa.repository.JpaRepository

interface ProductVariantRepository : JpaRepository<ProductVariant, Long>
