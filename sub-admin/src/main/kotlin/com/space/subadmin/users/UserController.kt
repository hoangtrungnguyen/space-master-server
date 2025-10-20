package com.space.subadmin.users

import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.util.UUID

@RestController
@RequestMapping("/api/users")
class UserController(private val userService: UserService) {

    @GetMapping
    fun getAllUsers(): List<User> = userService.getAllUsers()

    @GetMapping("/{uuid}")
    fun getUserById(@PathVariable uuid: UUID): ResponseEntity<User> {
        return userService.findByUUID(uuid)
            ?.let { ResponseEntity.ok(it) }
            ?: ResponseEntity.notFound().build()
    }

    @PostMapping
    fun createUser(@RequestBody user: User): User = userService.save(user)

    @PutMapping("/{id}")
    fun updateUser(@PathVariable id: UUID, @RequestBody userDetails: User): ResponseEntity<User> {
        return userService.findByUUID(id)?.let { existingUser ->
            // Assuming User is a data class, copy is preferred for immutability
            val updatedUser = existingUser.copy(
                username = userDetails.username,
                // Be cautious: password should be re-encoded if changed
                passwordHash = userDetails.passwordHash,
                role = userDetails.role
            )
            ResponseEntity.ok(userService.save(updatedUser))
        } ?: ResponseEntity.notFound().build()
    }
}
