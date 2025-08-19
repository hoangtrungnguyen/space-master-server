package com.ideaspace.config

import com.ideaspace.core.repository.CrudDocumentRepository
import com.ideaspace.core.repository.ElementRepo
import com.ideaspace.workers.DocumentStorage
import io.ktor.server.config.MapApplicationConfig
import io.ktor.server.plugins.di.dependencies
import io.ktor.server.plugins.di.resolve
import io.ktor.server.testing.testApplication
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Test
import org.testcontainers.containers.PostgreSQLContainer
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers

@Testcontainers // 👈 Annotation to enable Testcontainers
class DatabasesTest {

    // Define the PostgreSQL container. It will be started before tests and stopped after.
    @Container
    private val postgresContainer = PostgreSQLContainer("postgres:16")
        .withDatabaseName("idea_space_main")
        .withUsername("postgres")
        .withPassword("postgres")

    @Test
    fun `test configureDatabases with a real Postgres container`() = testApplication {
        environment {
            // Use the dynamic properties from the running container
            config = MapApplicationConfig(
                "postgres.driver" to "org.postgresql.Driver",
                "postgres.url" to postgresContainer.jdbcUrl,
                "postgres.user" to postgresContainer.username,
                "postgres.password" to postgresContainer.password
            )
        }
        application {
            // Given: The application is configured with the database
            configureDatabases()

            // When & Then: Verify that all database-related dependencies are available
            val documentStorage = dependencies.resolve<DocumentStorage>()
            assertNotNull(documentStorage, "DocumentStorage should be provided")

            val crudDocumentRepository = dependencies.resolve<CrudDocumentRepository>()
            assertNotNull(crudDocumentRepository, "CrudDocumentRepository should be provided")

            val elementRepo = dependencies.resolve<ElementRepo>()
            assertNotNull(elementRepo, "ElementRepo should be provided")
        }
    }
}