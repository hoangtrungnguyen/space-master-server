package com.space.subadmin.brand

import com.space.subadmin.products.Brand
import org.springframework.data.jpa.repository.JpaRepository
import java.util.*

interface BrandRepository : JpaRepository<Brand, UUID> {
}