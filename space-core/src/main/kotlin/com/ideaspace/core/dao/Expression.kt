package com.ideaspace.com.ideaspace.core.dao

import org.jetbrains.exposed.v1.core.Expression
import org.jetbrains.exposed.v1.core.QueryBuilder
import org.jetbrains.exposed.v1.jdbc.transactions.TransactionManager
import java.util.UUID
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

@OptIn(ExperimentalTime::class)
class DatabaseUUID : Expression<UUID>(){
    override fun toQueryBuilder(queryBuilder: QueryBuilder) {
        val function = when(val vendor = TransactionManager.current().db.vendor) {
            "H2" -> "RANDOM_UUID()"
            "PostgreSQL" -> "gen_random_uuid()"
            else -> error("Unsupported database vendor: $vendor")
        }
        queryBuilder.append(function)
    }
}

@OptIn(ExperimentalTime::class)
class CurrentTimestamp : Expression<Instant>() {
    override fun toQueryBuilder(queryBuilder: QueryBuilder) {
        val function = when (val vendor = TransactionManager.current().db.vendor) {
            "H2", "PostgreSQL" -> "CURRENT_TIMESTAMP"
            else -> error("Unsupported database vendor for CurrentTimestamp: $vendor")
        }
        queryBuilder.append(function)
    }
}