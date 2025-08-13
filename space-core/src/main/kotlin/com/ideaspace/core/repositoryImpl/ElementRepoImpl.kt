package com.ideaspace.core.repositoryImpl

import com.ideaspace.core.dao.ElementDAO
import com.ideaspace.core.dao.ElementTable
import com.ideaspace.core.dao.toEntity
import com.ideaspace.core.repository.ElementRepo
import com.ideaspace.core.models.Element
import kotlinx.coroutines.runBlocking
import org.jetbrains.exposed.v1.jdbc.Database
import org.jetbrains.exposed.v1.jdbc.SchemaUtils
import org.jetbrains.exposed.v1.jdbc.transactions.transaction
import java.util.UUID
import kotlin.Long
import kotlin.time.ExperimentalTime


@OptIn(ExperimentalTime::class)
class ElementRepoImpl(val db: Database) : ElementRepo {

    init {
        runBlocking {
            transaction(db) {
                SchemaUtils.create(ElementTable)
            }
        }
    }

    override suspend fun create(element: Element): Element = transaction(db) {
        val generated = ElementDAO.new {
            docId = element.docId
            parentUuid = element.parentUuid
            metadata = element.metadata
            type = element.type
            value = element.value
            deletedAt = element.deletedAt
        }

        element.uuid = generated.uuid.value
        return@transaction element
    }

    override suspend fun findByUuid(uuid: UUID): Element?  = transaction(db) {
        return@transaction ElementDAO.findById(uuid)?.toEntity()
    }

    override suspend fun listByDocId(docId: Long): List<Element>  = transaction(db) {
        return@transaction ElementDAO
            .find { ElementTable.docId eq docId }
            .toList()
            .map(ElementDAO::toEntity)
    }

}