package com.space.subadmin.category

import com.space.subadmin.products.Category
import org.springframework.data.jpa.repository.JpaRepository
import java.util.*

interface CategoryRepository : JpaRepository<Category, UUID> {
}