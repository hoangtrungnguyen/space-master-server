@file:OptIn(ExperimentalTime::class)

package com.ideaspace.core.dao

import com.ideaspace.core.models.DocumentStatus
import com.ideaspace.core.models.DocumentType
import kotlinx.serialization.Serializable
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
    val revId = long("rev_id")
    val title = varchar("title", 50)
    val creatorId = long("creator_id")
    val ownerId = long("owner_id")
    val createdAt = timestamp("created_at").defaultExpression(CurrentTimestamp())
    val lastModifiedAt = timestamp("last_modified_at").defaultExpression(CurrentTimestamp())
    val metadata = jsonb<JsonElement>(
        "metadata",
        serialize = { Json.encodeToString(JsonElement.serializer(), it) },
        deserialize = { Json.decodeFromString(JsonElement.serializer(), it) }
    ).nullable()
    val documentType = enumerationByName<DocumentType>("document_type", 50)
    val status = enumerationByName<DocumentStatus>("status", 50)
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
