package com.space.subadmin.users

import com.space.subadmin.db.Role
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/roles")
class RoleController(private val roleService: RoleService) {

    @GetMapping
    fun getAllRoles(): List<Role> = roleService.findAll()
}
