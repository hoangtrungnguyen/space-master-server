package com.space.subadmin

import com.space.subadmin.users.UserRepository
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.event.ApplicationReadyEvent
import org.springframework.boot.runApplication
import org.springframework.context.ApplicationListener
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Component

@SpringBootApplication
open class SubAdminApplication

fun main(args: Array<String>) {
    runApplication<SubAdminApplication>(*args)
}

@Component
class ApplicationStartup(
    private val userRepository: UserRepository,
    private val passwordEncoder: PasswordEncoder
) : ApplicationListener<ApplicationReadyEvent> {

    override fun onApplicationEvent(event: ApplicationReadyEvent) {
        // This is a good place to run seed data or other startup logic.
        // For now, it's empty.
    }
}
