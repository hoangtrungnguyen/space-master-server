package com.space.subadmin.users

import com.space.subadmin.db.User
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.core.userdetails.UsernameNotFoundException
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import java.util.UUID

@Service
class UserService(
    private val userRepository: UserRepository,
    private val passwordEncoder: PasswordEncoder
) : UserDetailsService {

    override fun loadUserByUsername(username: String): UserDetails {
        val user = userRepository.findByUsername(username)
            ?: throw UsernameNotFoundException("User not found with username: $username")

        return org.springframework.security.core.userdetails.User(
            user.username,
            user.passwordHash,
            listOf(SimpleGrantedAuthority("ROLE_${user.role.name}"))
        )
    }

    fun getAllUsers(): List<User> {
        return userRepository.findAll()
    }

    fun findById(id: Long): User? {
        return userRepository.findById(id).orElse(null)
    }

    fun findByUUID(uuid: UUID): User? {
        return userRepository.findByUuid(uuid)
    }
    fun findByUsername(username: String): User? {
        return userRepository.findByUsername(username)
    }

    fun createUser(user: User): User {
        userRepository.findByUsername(user.username)?.let {
            throw IllegalStateException("Username '${user.username}' is already taken.")
        }
        val userWithEncodedPassword = user.copy(
            passwordHash = passwordEncoder.encode(user.passwordHash)
        )
        return userRepository.save(userWithEncodedPassword)
    }

    fun save(user: User): User {
        // When updating, check if the new username is already taken by another user.
        userRepository.findByUsername(user.username)?.let { existingUser ->
            if (existingUser.id != user.id) {
                throw IllegalStateException("Username '${user.username}' is already taken.")
            }
        }
        // Note: This method assumes the password is not being changed or is already encoded.
        return userRepository.save(user)
    }

    fun deleteById(id: Long) {
        userRepository.deleteById(id)
    }

}
