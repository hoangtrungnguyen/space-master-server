package com.ideaspace.document

import io.ktor.server.plugins.di.DependencyRegistry
import org.example.com.ideaspace.core.repository.CrudDocumentRepository
import org.example.com.ideaspace.core.repositoryImpl.CrudDocumentRepositoryImpl


fun DependencyRegistry.provideDocumentDependencies() {

    provide<CrudDocumentRepository> {
        CrudDocumentRepositoryImpl()
    }
}
