package com.ideaspace.config

import com.ideaspace.core.datasources.postgres.connectToPostgresJDBC
import io.ktor.server.application.*
import io.ktor.server.plugins.di.dependencies
import com.ideaspace.core.repository.CrudDocumentRepository
import com.ideaspace.core.repositoryImpl.CrudDocumentRepositoryImpl

fun Application.configureDatabases() {
    val db = connectToPostgresJDBC(embedded = false)
    val repository =  CrudDocumentRepositoryImpl(db)
    dependencies {
        provide<CrudDocumentRepository>{
            repository
        }
    }
}

