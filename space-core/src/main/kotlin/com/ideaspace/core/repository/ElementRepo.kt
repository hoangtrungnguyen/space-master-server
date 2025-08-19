package com.ideaspace.core.repository

import com.ideaspace.core.dao.ElementDAO
import com.ideaspace.core.models.Element
import kotlinx.serialization.json.JsonElement
import java.util.UUID

interface ElementRepo {

    suspend fun create(element: Element): Element
    suspend fun insert(element: Element): Element
    suspend fun findByUuid(uuid: UUID): Element?
    suspend fun listByDocId(docId: Long): List<Element>
    suspend fun findAllByDocId(docId: Long): List<Element>

    suspend fun updateEditedElement(
        uuid: UUID,
        metadata: JsonElement,
        value: JsonElement,
        type: String,
    ): Element

    suspend fun deleteByUuid(
        uuid: UUID
    ): Boolean

    suspend fun updateMovedElement(
        uuid: UUID,
        parentUuid: UUID?
    ) : Int

}