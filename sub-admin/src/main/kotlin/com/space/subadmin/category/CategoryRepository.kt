package com.space.subadmin.category

import com.space.subadmin.db.Category
import org.springframework.data.jpa.repository.JpaRepository
import java.util.*

interface CategoryRepository : JpaRepository<Category, UUID> {
}