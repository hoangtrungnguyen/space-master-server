package com.ideaspace.core.repository

import com.ideaspace.core.models.User

interface UserRepo {
    fun findById(id: Long): User?
    fun findByLoginName(loginName: String): User?
    fun create(loginName: String, fullName: String): User
    fun update(id: Long, loginName: String): User?
}
