package com.ideaspace.config

import com.ideaspace.core.datasources.postgres.connectToPostgresJDBC
import com.ideaspace.core.dao.DocumentTable
import io.ktor.server.application.*
import io.ktor.server.plugins.di.dependencies
import com.ideaspace.core.repository.CrudDocumentRepository
import com.ideaspace.core.repository.ElementRepo
import com.ideaspace.core.repositoryImpl.CrudDocumentRepositoryImpl
import com.ideaspace.core.repositoryImpl.ElementRepoImpl
import com.ideaspace.workers.DocumentStorage
import org.jetbrains.exposed.v1.jdbc.SchemaUtils
import org.jetbrains.exposed.v1.jdbc.transactions.transaction
import java.util.concurrent.ConcurrentHashMap

fun Application.configureDatabases() {

    val db = connectToPostgresJDBC(embedded = false)

    val documentStorage = DocumentStorage(
        ConcurrentHashMap()
    )

    dependencies {

        provide<DocumentStorage>(){
            documentStorage
        }

        provide<CrudDocumentRepository>{
            CrudDocumentRepositoryImpl(db)
        }

        provide<ElementRepo>(){
            ElementRepoImpl(db)
        }
    }

}
