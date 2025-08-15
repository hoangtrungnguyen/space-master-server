@file:OptIn(ExperimentalTime::class)

package com.ideaspace.core.dao

import com.ideaspace.com.ideaspace.core.dao.CurrentTimestamp
import com.ideaspace.com.ideaspace.core.dao.DatabaseUUID
import com.ideaspace.core.models.BusinessDocument
import com.ideaspace.core.models.DocumentStatus
import com.ideaspace.core.models.DocumentType
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import org.jetbrains.exposed.v1.core.dao.id.EntityID
import org.jetbrains.exposed.v1.core.dao.id.LongIdTable
import org.jetbrains.exposed.v1.dao.LongEntity
import org.jetbrains.exposed.v1.dao.LongEntityClass
import org.jetbrains.exposed.v1.datetime.timestamp
import org.jetbrains.exposed.v1.json.jsonb
import kotlin.time.ExperimentalTime

object DocumentTable: LongIdTable("document") {
    val uuid = uuid("uuid").defaultExpression(DatabaseUUID()).uniqueIndex()
    val revId = long("rev_id")
    val title = varchar("title", 255)
    val creatorId = long("creator_id")
    val ownerId = long("owner_id")
    val createdAt = timestamp("created_at").defaultExpression(CurrentTimestamp())
    val lastModifiedAt = timestamp("last_modified_at").defaultExpression(CurrentTimestamp())
    val metadata = jsonb<JsonElement>("metadata", Json, JsonElement.serializer()).nullable()
    val documentType = enumerationByName<DocumentType>("document_type", 255)
    val status = enumerationByName<DocumentStatus>("status", 255)
    val transformVersion = long("transform_version")
    val kafkaOffset = long("kafka_offset")
}

class DocumentDAO(id: EntityID<Long>) : LongEntity(id) {
    companion object : LongEntityClass<DocumentDAO>(DocumentTable)

    var uuid by DocumentTable.uuid
    var revId by DocumentTable.revId
    var title by DocumentTable.title
    var creatorId by DocumentTable.creatorId
    var ownerId  by DocumentTable.ownerId
    var createdAt by DocumentTable.createdAt
    var lastModifiedAt by DocumentTable.lastModifiedAt
    var metadata by DocumentTable.metadata
    var documentType by DocumentTable.documentType
    var status by DocumentTable.status
    var transformVersion by DocumentTable.transformVersion
    var kafkaOffset by DocumentTable.kafkaOffset

}

fun DocumentDAO.toModel() : BusinessDocument {
    return BusinessDocument(
        id = this.id.value,
        uuid = this.uuid,
        revId = this.revId,
        title = this.title,
        creatorId = this.creatorId,
        ownerId = this.ownerId,
        createdAt = this.createdAt,
        lastModifiedAt = this.lastModifiedAt,
        metadata = this.metadata,
        documentType = this.documentType,
        status = this.status,
        transformVersion = this.transformVersion,
        kafkaOffset = this.kafkaOffset
    )
}