package com.space.subadmin.users

import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.util.UUID

@RestController
@RequestMapping("/api/users")
class UserController(private val userService: UserService) {

    @GetMapping
    fun getAllUsers(): List<User> = userService.getAllUsers()

    @GetMapping("/{id}")
    fun getUserById(@PathVariable id: UUID): ResponseEntity<User> {
        val user = userService.findById(id)
        return if (user != null) {
            ResponseEntity.ok(user)
        } else {
            ResponseEntity.notFound().build()
        }
    }

    @PostMapping
    fun createUser(@RequestBody user: User): User = userService.save(user)

    @PutMapping("/{id}")
    fun updateUser(@PathVariable id: UUID, @RequestBody userDetails: User): ResponseEntity<User> {
        val existingUser = userService.findById(id)
        return if (existingUser != null) {
            val updatedUser = existingUser.apply {
                username = userDetails.username
                passwordHash = userDetails.passwordHash
                role = userDetails.role
            }
            ResponseEntity.ok(userService.save(updatedUser))
        } else {
            ResponseEntity.notFound().build()
        }
    }

    @DeleteMapping("/{id}")
    fun deleteUser(@PathVariable id: UUID): ResponseEntity<Void> {
        userService.deleteById(id)
        return ResponseEntity.noContent().build()
    }
}
