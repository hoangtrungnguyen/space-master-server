package com.space.subadmin

import com.space.subadmin.users.Role
import com.space.subadmin.users.User
import com.space.subadmin.users.UserRepository
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.event.ApplicationReadyEvent
import org.springframework.boot.runApplication
import org.springframework.context.ApplicationListener
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Component
import java.util.UUID

@SpringBootApplication
class SubAdminApplication

fun main(args: Array<String>) {
    runApplication<SubAdminApplication>(*args)
}

@Component
class ApplicationStartup(
    private val userRepository: UserRepository,
    private val passwordEncoder: PasswordEncoder
) : ApplicationListener<ApplicationReadyEvent> {

    override fun onApplicationEvent(event: ApplicationReadyEvent) {
//                val adminUser = User(
//                    id = UUID.randomUUID(),
//                    username = "admin",
//                    passwordHash = passwordEncoder.encode("123"),
//                    role = Role.ADMIN
//                )
//                userRepository.save(adminUser)

    }
}
