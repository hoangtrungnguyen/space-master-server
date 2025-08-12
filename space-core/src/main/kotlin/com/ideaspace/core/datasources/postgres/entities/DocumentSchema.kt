package com.ideaspace.core.datasources.postgres.entities

import org.jetbrains.exposed.v1.core.Expression
import org.jetbrains.exposed.v1.core.QueryBuilder
import org.jetbrains.exposed.v1.core.dao.id.EntityID
import org.jetbrains.exposed.v1.core.dao.id.LongIdTable
import org.jetbrains.exposed.v1.dao.LongEntity
import org.jetbrains.exposed.v1.dao.LongEntityClass
import org.jetbrains.exposed.v1.jdbc.transactions.TransactionManager
import java.util.UUID

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
object  DocumentTable: LongIdTable("document") {
    val name = varchar("name", 50)
    val uuid = uuid("uuid").defaultExpression(DatabaseUUID()).uniqueIndex()
}

class DocumentDAO(id: EntityID<Long>) : LongEntity(id) {
    companion object : LongEntityClass<DocumentDAO>(DocumentTable)

    var name by DocumentTable.name
    var uuid by DocumentTable.uuid

}
