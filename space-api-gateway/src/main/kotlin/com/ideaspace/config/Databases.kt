package com.ideaspace.config

import com.ideaspace.com.ideaspace.core.repository.ElementRepo
import com.ideaspace.com.ideaspace.core.repositoryImpl.ElementRepoImpl
import com.ideaspace.core.datasources.postgres.connectToPostgresJDBC
import io.ktor.server.application.*
import io.ktor.server.plugins.di.dependencies
import com.ideaspace.core.repository.CrudDocumentRepository
import com.ideaspace.core.repositoryImpl.CrudDocumentRepositoryImpl

fun Application.configureDatabases() {
    val db = connectToPostgresJDBC(embedded = false)
    dependencies {
        provide<CrudDocumentRepository>{
            CrudDocumentRepositoryImpl(db)
        }
        provide<ElementRepo> {
            ElementRepoImpl(db)
        }
    }
}

