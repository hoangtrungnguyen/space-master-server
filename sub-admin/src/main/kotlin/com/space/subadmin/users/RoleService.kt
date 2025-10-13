package com.space.subadmin.users

import org.springframework.stereotype.Service

@Service
class RoleService {

    fun findAll(): List<Role> = Role.entries
}
