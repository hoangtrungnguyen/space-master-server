package com.ideaspace.core.datasources.postgres

import io.ktor.server.application.Application
import io.ktor.server.application.log
import org.h2.tools.Server
import org.jetbrains.exposed.v1.jdbc.Database


fun Application.connectToPostgresJDBC(embedded: Boolean): Database{
    if (embedded) {
        log.info("Using embedded H2 database for testing; replace this flag to use postgres")
        val server = Server.createWebServer("-web", "-webAllowOthers", "-webPort", "8082").start()
        log.info("H2 console is running at: ${server.url}")
        log.info("Use JDBC URL: jdbc:h2:mem:test and user: sa to connect.")
        val h2db = Database.connect("jdbc:h2:mem:test;DB_CLOSE_DELAY=-1;", driver = "org.h2.Driver")
        return h2db
    } else {
        val url = environment.config.property("postgres.url").getString()
        log.info("Connecting to postgres database at $url")
        val user = environment.config.property("postgres.user").getString()
        val password = environment.config.property("postgres.password").getString()
        val postgresqldb = Database.connect(
            url,
            driver = "org.postgresql.Driver",
            user = user,
            password = password
        )
        return postgresqldb
    }
}
