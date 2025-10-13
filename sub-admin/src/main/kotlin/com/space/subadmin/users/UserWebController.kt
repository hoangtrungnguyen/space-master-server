package com.space.subadmin.users

import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.ModelAttribute
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping

@Controller
@RequestMapping("/users")
class UserWebController(
    private val userService: UserService,
    private val roleService: RoleService
) {

    @GetMapping("/list")
    fun listUsers(model: Model): String {
        model.addAttribute("users", userService.getAllUsers())
        return "users/users-list"
    }

    @GetMapping("/add")
    fun showAddUserForm(model: Model): String {
        model.addAttribute("user", User(username = "", passwordHash = "", role = Role.STAFF))
        model.addAttribute("roles", roleService.findAll())
        return "users/add-user"
    }

    @PostMapping("/add")
    fun addUser(@ModelAttribute user: User): String {
        userService.createUser(user)
        return "redirect:/users/list"
    }
}
