package org.example.com.ideaspace.core.repository

interface DocumentRepository {
}


interface CrudDocumentRepository : DocumentRepository{
    /**
     * Creates a new space and persists it.
     */
    suspend fun create()

    /**
     * Finds a space by its unique identifier.
     * @param id The unique ID of the space.
     * @return The found SpaceModel, or null if no space with the given ID exists.
     */
    suspend fun findById(id: String): Any

    /**
     * Updates an existing space.
     * @param id The unique ID of the space to update.
     * @param space The new data for the space.
     * @return The updated SpaceModel, or null if no space with the given ID exists.
     */
    suspend fun update(id: String, space: Any): Any?

    suspend fun findAll(): List<Any>
}