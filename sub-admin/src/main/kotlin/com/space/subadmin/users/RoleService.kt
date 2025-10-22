package com.space.subadmin.users

import com.space.subadmin.db.Role
import org.springframework.stereotype.Service

@Service
class RoleService {

    fun findAll(): List<Role> = Role.entries
}
