package com.ideaspace.config

import com.ideaspace.core.datasources.postgres.connectToPostgresJDBC
import com.ideaspace.core.dao.DocumentTable
import io.ktor.server.application.*
import io.ktor.server.plugins.di.dependencies
import com.ideaspace.core.repository.CrudDocumentRepository
import com.ideaspace.core.repositoryImpl.CrudDocumentRepositoryImpl
import org.jetbrains.exposed.v1.jdbc.SchemaUtils
import org.jetbrains.exposed.v1.jdbc.transactions.transaction

fun Application.configureDatabases() {

    val db = connectToPostgresJDBC(embedded = false)
    val repository =  CrudDocumentRepositoryImpl()
    dependencies {
        provide<CrudDocumentRepository>{
        repository
        }
    }

    // In development mode, drop all tables on application stop to start with a clean slate.
    if (environment.config.property("developmentMode").getString().toBoolean()) {
        log.info("Development mode: tables will be dropped on application shutdown.")
        environment.monitor.subscribe(ApplicationStopPreparing) {
            transaction(db) {
                log.info("Dropping database tables...")
                // NOTE: Add all your Exposed Table objects here to drop them on shutdown.
                // For example, if you also have a Users table:
                // SchemaUtils.drop(Documents, Users)
                SchemaUtils.drop(DocumentTable)
                log.info("Database tables dropped.")
            }
        }
    }

}
