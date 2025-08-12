@file:OptIn(ExperimentalTime::class)

package com.ideaspace.core.datasources.postgres.entities

import org.jetbrains.exposed.v1.core.Expression
import org.jetbrains.exposed.v1.core.QueryBuilder
import org.jetbrains.exposed.v1.core.dao.id.EntityID
import org.jetbrains.exposed.v1.core.dao.id.LongIdTable
import org.jetbrains.exposed.v1.dao.LongEntity
import org.jetbrains.exposed.v1.dao.LongEntityClass
import org.jetbrains.exposed.v1.datetime.timestamp
import org.jetbrains.exposed.v1.jdbc.transactions.TransactionManager
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
    val title = varchar("title", 50)
    val uuid = uuid("uuid").defaultExpression(DatabaseUUID()).uniqueIndex()
    val createdAt = timestamp("created_at").defaultExpression(CurrentTimestamp())
    val updatedAt = timestamp("updated_at").nullable()
    val kafkaOffset = integer("kafka_offset").default(0)
}

class DocumentDAO(id: EntityID<Long>) : LongEntity(id) {
    companion object : LongEntityClass<DocumentDAO>(DocumentTable)

    var title by DocumentTable.title
    var uuid by DocumentTable.uuid
    var createdAt by DocumentTable.createdAt
    var kafkaOffset by DocumentTable.kafkaOffset
    var updatedAt by DocumentTable.updatedAt

}
