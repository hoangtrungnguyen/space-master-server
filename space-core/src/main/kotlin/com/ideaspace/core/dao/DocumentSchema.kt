@file:OptIn(ExperimentalTime::class)

package com.ideaspace.core.dao

import kotlinx.serialization.json.JsonElement
import org.jetbrains.exposed.v1.core.Expression
import org.jetbrains.exposed.v1.core.QueryBuilder
import org.jetbrains.exposed.v1.core.dao.id.EntityID
import org.jetbrains.exposed.v1.core.dao.id.LongIdTable
import org.jetbrains.exposed.v1.dao.LongEntity
import org.jetbrains.exposed.v1.dao.LongEntityClass
import org.jetbrains.exposed.v1.datetime.timestamp
import org.jetbrains.exposed.v1.jdbc.transactions.TransactionManager
//import org.jetbrains.exposed.v1.
import kotlinx.serialization.json.Json
import org.jetbrains.exposed.v1.json.jsonb
import java.util.UUID
import kotlin.time.ExperimentalTime
import kotlin.time.Instant


class DatabaseUUID : Expression<UUID>(){
    override fun toQueryBuilder(queryBuilder: QueryBuilder) {
        val function = when(val vendor = TransactionManager.current().db.vendor){
            "H2" -> "RANDOM_UUID()"
            "PostgreSQL" -> "gen_random_uuid()"
            else -> error("Unsupported database vendor: $vendor")
        }
        queryBuilder.append(function)
    }
}

class CurrentTimestamp : Expression<Instant>() {
    override fun toQueryBuilder(queryBuilder: QueryBuilder) {
        val function = when (val vendor = TransactionManager.current().db.vendor) {
            "H2", "PostgreSQL" -> "CURRENT_TIMESTAMP"
            else -> error("Unsupported database vendor for CurrentTimestamp: $vendor")
        }
        queryBuilder.append(function)
    }
}

object DocumentTable: LongIdTable("document") {
    val uuid = uuid("uuid").defaultExpression(DatabaseUUID()).uniqueIndex()
    val revId = long("rev_id").default(0)
    val title = varchar("title", 50)
    val creatorId = long("creator_id").nullable()
    val ownerId = long("owner_id").nullable() // owner_id
    val createdAt = timestamp("created_at").defaultExpression(CurrentTimestamp())
    val updatedAt = timestamp("updated_at").nullable()
    val lastModifiedAt = timestamp("last_modified_at").nullable() // last_modified_at
    val metadata = jsonb<JsonElement>(
        "metadata",
        serialize = { Json.encodeToString(JsonElement.serializer(), it) },
        deserialize = { Json.decodeFromString(JsonElement.serializer(), it) }
    ).nullable()
    val documentType = varchar("document_type", 50).default("WORD")
    val status = enumerationByName<DocumentStatus>("status", 50).default(DocumentStatus.DRAFT)
    val transformVersion = long("transform_version").default(0)
    val kafkaOffset = integer("kafka_offset").default(0)
}

enum class DocumentStatus{
    ARCHIVE, DRAFT, PUBLISH
}

class DocumentDAO(id: EntityID<Long>) : LongEntity(id) {
    companion object : LongEntityClass<DocumentDAO>(DocumentTable)

    var title by DocumentTable.title
    var uuid by DocumentTable.uuid
    var createdAt by DocumentTable.createdAt
    var kafkaOffset by DocumentTable.kafkaOffset
    var updatedAt by DocumentTable.updatedAt
    var metadata by DocumentTable.metadata
    var documentType by DocumentTable.documentType
    var status by DocumentTable.status
    var transformVersion by DocumentTable.transformVersion
    var creatorId by DocumentTable.creatorId
    var ownerId  by DocumentTable.ownerId
    var revId by DocumentTable.revId
    var lastModifiedAt by DocumentTable.lastModifiedAt

}
