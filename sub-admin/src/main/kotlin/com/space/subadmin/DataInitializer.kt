package com.space.subadmin

import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.core.io.ClassPathResource
import org.springframework.util.FileCopyUtils
import java.nio.charset.StandardCharsets

import com.space.subadmin.customers.CustomerService
import com.space.subadmin.db.Customer
import com.space.subadmin.db.Role
import com.space.subadmin.db.User
import com.space.subadmin.users.UserService
import org.slf4j.LoggerFactory
import org.springframework.boot.context.event.ApplicationReadyEvent
import org.springframework.context.ApplicationListener
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Component

@Component
class DataInitializer(
    private val userService: UserService,
    private val customerService: CustomerService,
    private val jdbcTemplate: JdbcTemplate
) : ApplicationListener<ApplicationReadyEvent> {

    private val logger = LoggerFactory.getLogger(DataInitializer::class.java)

    override fun onApplicationEvent(event: ApplicationReadyEvent) {
        logger.info("Checking for admin user...")
        when (userService.findByUsername("admin")) {
            null -> {
                logger.info("Admin user not found, creating a new one.")
                val newAdmin = User(
                    username = "admin",
                    passwordHash = "123", // In a real app, use a secure password
                    role = Role.ADMIN
                )
                userService.createUser(newAdmin)
                logger.info("Admin user created successfully.")
            }

            else -> logger.info("Admin user already exists.")
        }

    }
}


@Component
@Profile("dev")
class DevProfileDataInitializer(
    private val userService: UserService,
    private val customerService: CustomerService,
    private val jdbcTemplate: JdbcTemplate
) : ApplicationListener<ApplicationReadyEvent> {
    private val logger = LoggerFactory.getLogger(DevProfileDataInitializer::class.java)

    override fun onApplicationEvent(event: ApplicationReadyEvent) {

        logger.info("Executing data.sql script...")
        try {
            val resource = ClassPathResource("data.sql")
            val sqlScript = String(FileCopyUtils.copyToByteArray(resource.inputStream), StandardCharsets.UTF_8)
            jdbcTemplate.execute(sqlScript)
            logger.info("data.sql script executed successfully.")
        } catch (e: Exception) {
            logger.error("Error executing data.sql script: ${e.message}", e)
        }
    }
}