@file:OptIn(ExperimentalTime::class)

package com.ideaspace.core.dao

import com.ideaspace.com.ideaspace.core.dao.CurrentTimestamp
import com.ideaspace.core.models.Process
import org.jetbrains.exposed.v1.core.dao.id.EntityID
import org.jetbrains.exposed.v1.core.dao.id.LongIdTable
import org.jetbrains.exposed.v1.dao.LongEntity
import org.jetbrains.exposed.v1.dao.LongEntityClass
import org.jetbrains.exposed.v1.datetime.timestamp
import kotlin.time.ExperimentalTime

object ProcessTable : LongIdTable("process") {
    val docId = long("doc_id").references(DocumentTable.id)
    val userId = long("user_id").references(UserTable.id)
    val windowId = long("window_id")
    val sessionId = long("session_id")
    val isActive = bool("is_active").default(true)
    val lastActiveAt = timestamp("last_active_at").defaultExpression(CurrentTimestamp())
    
    // Unique constraint on (doc_id, user_id, window_id)
    init {
        uniqueIndex(docId, userId, windowId)
    }
}

class ProcessDAO(id: EntityID<Long>) : LongEntity(id) {
    companion object : LongEntityClass<ProcessDAO>(ProcessTable)

    var docId by ProcessTable.docId
    var userId by ProcessTable.userId
    var windowId by ProcessTable.windowId
    var sessionId by ProcessTable.sessionId
    var isActive by ProcessTable.isActive
    var lastActiveAt by ProcessTable.lastActiveAt
}

fun ProcessDAO.toModel(): Process {
    return Process(
        id = this.id.value,
        docId = this.docId,
        userId = this.userId,
        windowId = this.windowId,
        sessionId = this.sessionId,
        isActive = this.isActive,
        lastActiveAt = this.lastActiveAt
    )
}