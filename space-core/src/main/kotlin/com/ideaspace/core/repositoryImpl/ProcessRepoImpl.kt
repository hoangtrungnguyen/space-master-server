@file:OptIn(ExperimentalTime::class)

package com.ideaspace.core.repositoryImpl

import com.ideaspace.core.dao.ProcessDAO
import com.ideaspace.core.dao.ProcessTable
import com.ideaspace.core.dao.toModel
import com.ideaspace.core.models.Process
import com.ideaspace.core.repository.ProcessRepo
import kotlinx.coroutines.runBlocking
import org.jetbrains.exposed.v1.core.and
import org.jetbrains.exposed.v1.jdbc.Database
import org.jetbrains.exposed.v1.jdbc.SchemaUtils
import org.jetbrains.exposed.v1.jdbc.transactions.transaction
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

class ProcessRepoImpl(val db: Database) : ProcessRepo {

    init {
        runBlocking {
            transaction(db) {
                SchemaUtils.create(ProcessTable)
            }
        }
    }

    override suspend fun create(process: Process): Process = transaction(db) {
        val generated = ProcessDAO.new {
            docId = process.docId
            userId = process.userId
            windowId = process.windowId
            sessionId = process.sessionId
            isActive = process.isActive
            lastActiveAt = process.lastActiveAt
        }
        generated.toModel()
    }

    override suspend fun findById(id: Long): Process? = transaction(db) {
        ProcessDAO.findById(id)?.toModel()
    }

    override suspend fun findByDocUserWindow(docId: Long, userId: Long, windowId: Long): Process? = transaction(db) {
        ProcessDAO.find { 
            (ProcessTable.docId eq docId) and 
            (ProcessTable.userId eq userId) and 
            (ProcessTable.windowId eq windowId) 
        }.firstOrNull()?.toModel()
    }

    override suspend fun findAll(): List<Process> = transaction(db) {
        ProcessDAO.all().map { it.toModel() }.toList()
    }

    @OptIn(ExperimentalTime::class)
    override suspend fun update(process: Process): Process? = transaction(db) {
        ProcessDAO.findById(process.id)?.also { dao ->
            dao.docId = process.docId
            dao.userId = process.userId
            dao.windowId = process.windowId
            dao.sessionId = process.sessionId
            dao.isActive = process.isActive
            dao.lastActiveAt = Clock.System.now()
        }?.toModel()
    }

    override suspend fun deleteById(id: Long): Boolean = transaction(db) {
        ProcessDAO.findById(id)?.let { dao ->
            dao.delete()
            true
        } ?: false
    }

    override suspend fun existsById(id: Long): Boolean = transaction(db) {
        ProcessDAO.findById(id) != null
    }

    override suspend fun existsByDocUserWindow(docId: Long, userId: Long, windowId: Long): Boolean = transaction(db) {
        !ProcessDAO.find { 
            (ProcessTable.docId eq docId) and 
            (ProcessTable.userId eq userId) and 
            (ProcessTable.windowId eq windowId) 
        }.empty()
    }
}