package com.ideaspace.document

import com.ideaspace.core.repository.CrudDocumentRepository
import com.ideaspace.core.repository.ElementRepo
import com.ideaspace.workers.DocumentStorage
import com.ideaspace.workers.LogPublisher
import io.ktor.server.application.Application
import io.ktor.server.plugins.di.dependencies
import io.ktor.server.plugins.di.resolve

fun Application.provideDocumentDI() {
    dependencies.provide {
        CommandFactory(
            documentRedisPublisher =  this.resolve<DocumentRedisPublisher>(),
            elementRepo = this.resolve<ElementRepo>(),
            documentStorage = this.resolve<DocumentStorage>(),
            logPublisher = this.resolve<LogPublisher>(),
            documentRepository = this.resolve<CrudDocumentRepository>()
        )
    }
}