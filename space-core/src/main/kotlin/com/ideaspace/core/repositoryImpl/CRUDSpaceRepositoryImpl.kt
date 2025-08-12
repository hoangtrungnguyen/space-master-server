package org.example.com.ideaspace.core.repositoryImpl

import org.example.com.ideaspace.core.repository.CrudDocumentRepository
import java.util.UUID


class CrudDocumentRepositoryImpl() : CrudDocumentRepository {
    override suspend fun create() {
        TODO("Not yet implemented")
    }

    override suspend fun findById(id: String): Any {
        TODO("Not yet implemented")
    }

    override suspend fun update(id: String, space: Any): Any? {
        TODO("Not yet implemented")
    }

    override suspend fun findAll(): List<Any> {
        TODO("Not yet implemented")
    }
}