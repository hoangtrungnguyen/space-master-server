package com.ideaspace.core.datasources.postgres

import io.ktor.network.sockets.Connection
import io.ktor.server.application.Application
import io.ktor.server.application.log
import io.r2dbc.h2.H2Connection
import io.r2dbc.spi.ConnectionFactoryOptions
import org.h2.tools.Server
import org.jetbrains.exposed.v1.jdbc.Database
import org.jetbrains.exposed.v1.r2dbc.R2dbcDatabase

fun Application.connectToPostgresR2dbc(embedded: Boolean): R2dbcDatabase {
    if (embedded) {
        log.info("Using embedded H2 database for testing; replace this flag to use postgres")
        val server = Server.createWebServer("-web", "-webAllowOthers", "-webPort", "8082").start()
        log.info("H2 console is running at: ${server.url}")
        log.info("Use JDBC URL: jdbc:h2:mem:test and user: sa to connect.")

        val h2db =  R2dbcDatabase.connect("r2dbc:h2:mem:///test;DB_CLOSE_DELAY=-1")
        return h2db
    } else {
        val postgresqldb = R2dbcDatabase.connect(
            url = "r2dbc:postgresql://localhost:5439/idea_space_main",
            databaseConfig = {
                connectionFactoryOptions {
                    option(ConnectionFactoryOptions.USER, "postgres")
                    option(ConnectionFactoryOptions.PASSWORD, "postgres")
                }
            }
        )
        return postgresqldb
    }
}

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
