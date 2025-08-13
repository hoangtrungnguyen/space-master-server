package com.ideaspace.core.repository

import com.ideaspace.core.models.Element
import java.util.UUID

interface ElementRepo {

    suspend fun create(element: Element): Element
    suspend fun findByUuid(uuid: UUID): Element?
    suspend fun listByDocId(docId: Long): List<Element>

}