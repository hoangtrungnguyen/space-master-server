package com.space.features.space.queries

import com.space.features.space.SpaceRepository
import com.space.features.space.commands.model.SpaceModel
import java.util.UUID
import kotlin.random.Random
import kotlinx.datetime.*


interface CRUDSpaceRepository : SpaceRepository {
    /**
     * Creates a new space and persists it.
     */
    suspend fun create(): SpaceModel

    /**
     * Finds a space by its unique identifier.
     * @param id The unique ID of the space.
     * @return The found SpaceModel, or null if no space with the given ID exists.
     */
    suspend fun findById(id: String): SpaceModel?

    /**
     * Updates an existing space.
     * @param id The unique ID of the space to update.
     * @param space The new data for the space.
     * @return The updated SpaceModel, or null if no space with the given ID exists.
     */
    suspend fun update(id: String, space: SpaceModel): SpaceModel?

    suspend fun findAll(): List<SpaceModel>
}


class CRUDSpaceRepositoryImpl() : CRUDSpaceRepository {

    // Using a Map is efficient for finding items by ID.
    // It's private to prevent direct access from outside the class.
    private val _mockSpaces = mutableMapOf<String, SpaceModel>()

    /**
     * Creates a new space.
     *
     * This mock implementation simulates creating a space by assigning a unique ID
     * and adding it to an in-memory map.
     *
     * @param space The space data to create. Any existing 'id' will be replaced.
     * @return The created SpaceModel with its new unique ID.
     */
    override suspend fun create(): SpaceModel {
        // Use copy() to create a new instance with a server-generated unique ID.
        val newSpace = SpaceModel(
            id = UUID.randomUUID().toString(),
            revision = 0,
            name = "${Clock.System.now().epochSeconds} newSpace"
        )
        _mockSpaces[newSpace.id] = newSpace
        return newSpace
    }

    /**
     * Finds a space by its unique ID from the in-memory map.
     *
     * @param id The unique ID of the space to find.
     * @return The corresponding SpaceModel, or null if it's not found.
     */
    override suspend fun findById(id: String): SpaceModel? {
        return _mockSpaces[id]
    }

    /**
     * Updates an existing space in the in-memory map.
     *
     * @param id The unique ID of the space to update.
     * @param space The object containing the new data for the space.
     * @return The updated SpaceModel, or null if no space with the given ID was found.
     */
    override suspend fun update(id: String, space: SpaceModel): SpaceModel? {
        // Check if the space exists before trying to update it.
        if (!_mockSpaces.containsKey(id)) {
            return null
        }

        // Ensure the ID of the updated object remains consistent.
        val updatedSpace = space.copy(id = id)
        _mockSpaces[id] = updatedSpace
        return updatedSpace
    }

    override suspend fun findAll(): List<SpaceModel> {
        return _mockSpaces.values.toList()
    }

}