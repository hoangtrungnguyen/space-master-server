package org.example.com.ideaspace.core.datasources.postgres

import io.ktor.server.application.Application
import io.ktor.server.application.log
import java.sql.Connection
import java.sql.DriverManager
import org.h2.tools.Server

fun Application.connectToPostgres(embedded: Boolean): Connection {
    if (embedded) {
        Class.forName("org.h2.Driver")
        log.info("Using embedded H2 database for testing; replace this flag to use postgres")
        val server = Server.createWebServer("-web", "-webAllowOthers", "-webPort", "8082").start()
        log.info("H2 console is running at: ${server.url}")
        log.info("Use JDBC URL: jdbc:h2:mem:test and user: sa to connect.")
        return DriverManager.getConnection("jdbc:h2:mem:test;DB_CLOSE_DELAY=-1", "sa", "")
    } else {
        Class.forName("org.postgresql.Driver")

        val url = environment.config.property("postgres.url").getString()
        log.info("Connecting to postgres database at $url")
        val user = environment.config.property("postgres.user").getString()
        val password = environment.config.property("postgres.password").getString()

        return DriverManager.getConnection(url, user, password)
    }
}
