package com.space.subadmin

import com.space.subadmin.users.Role
import com.space.subadmin.users.User
import com.space.subadmin.users.UserService
import org.slf4j.LoggerFactory
import org.springframework.boot.context.event.ApplicationReadyEvent
import org.springframework.context.ApplicationListener
import org.springframework.stereotype.Component
import java.util.UUID

@Component
class DataInitializer(
    private val userService: UserService
) : ApplicationListener<ApplicationReadyEvent> {

    private val logger = LoggerFactory.getLogger(DataInitializer::class.java)

    override fun onApplicationEvent(event: ApplicationReadyEvent) {
        logger.info("Checking for admin user...")
        val adminUser = userService.findByUsername("admin")
        if (adminUser == null) {
            logger.info("Admin user not found, creating a new one.")
            val newAdmin = User(
                id = UUID.randomUUID(),
                username = "admin",
                passwordHash = "123", // In a real app, use a secure password
                role = Role.ADMIN
            )
            userService.createUser(newAdmin)
            logger.info("Admin user created successfully.")
        } else {
            logger.info("Admin user already exists.")
        }
    }
}
