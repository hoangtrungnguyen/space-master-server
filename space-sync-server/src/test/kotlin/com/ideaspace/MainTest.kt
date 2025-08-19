package com.ideaspace

import com.ideaspace.config.configureDatabases
import com.ideaspace.config.configureRouting
import com.ideaspace.core.repository.CrudDocumentRepository
import com.ideaspace.core.repository.ElementRepo
import com.ideaspace.workers.DocumentStorage
import io.ktor.client.request.get
import io.ktor.client.statement.bodyAsText
import io.ktor.http.HttpStatusCode
import io.ktor.server.plugins.di.dependencies
import io.ktor.server.testing.testApplication
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class MainTest {
    @Test
    fun testRoot() = testApplication {
        application {
            configureRouting()
        }
        val response = client.get("/")
        assertEquals(HttpStatusCode.OK, response.status)
        assertEquals("Hello World!", response.bodyAsText())
    }

    @Test
    fun `test configureDatabases`() = testApplication {
        environment {

        }
        application {
            // Given
            configureDatabases()

            // When & Then
            val documentStorage = dependencies.resolve<DocumentStorage>()
            assertNotNull(documentStorage)

            val crudDocumentRepository = dependencies.resolve<CrudDocumentRepository>()
            assertNotNull(crudDocumentRepository)

            val elementRepo = dependencies.resolve<ElementRepo>()
            assertNotNull(elementRepo)
        }
    }


}