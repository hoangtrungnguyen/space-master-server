package com.space.subadmin.users

import org.springframework.data.jpa.repository.JpaRepository
import java.util.UUID

interface UserRepository : JpaRepository<User, Long> {
    fun findByUsername(username: String): User?
    fun findByUuid(uuid: UUID): User?
}
