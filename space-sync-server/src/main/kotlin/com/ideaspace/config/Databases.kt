package com.ideaspace.config


import io.ktor.server.application.*
import org.example.com.ideaspace.core.datasources.postgres.connectToPostgres
import java.sql.Connection

fun Application.configureDatabases() {
    val dbConnection: Connection = connectToPostgres(embedded = true)
}
