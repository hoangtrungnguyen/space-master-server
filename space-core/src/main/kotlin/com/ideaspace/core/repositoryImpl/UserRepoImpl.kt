package com.ideaspace.core.repositoryImpl

import com.ideaspace.core.dao.UserDAO
import com.ideaspace.core.dao.UserTable
import com.ideaspace.core.models.User
import com.ideaspace.core.repository.UserRepo
import kotlinx.coroutines.runBlocking
import org.jetbrains.exposed.v1.jdbc.Database
import org.jetbrains.exposed.v1.jdbc.SchemaUtils
import org.jetbrains.exposed.v1.jdbc.transactions.transaction

class UserRepoImpl(val db: Database) : UserRepo {
    init {
        runBlocking {
            transaction(db) {
                SchemaUtils.create(UserTable)
            }
        }
    }
    override fun findById(id: Long): User? = transaction(db) {
        UserDAO.findById(id)?.toModel()
    }

    override fun findByLoginName(loginName: String): User? = transaction(db) {
        UserDAO.find { UserTable.loginName eq loginName }.firstOrNull()?.toModel()
    }

    override fun create(loginName: String, fullName: String): User = transaction(db) {
        UserDAO.new {
            this.loginName = loginName
            this.fullName = fullName
        }.toModel()
    }

    override fun update(id: Long, loginName: String): User? = transaction(db) {
        UserDAO.findById(id)?.apply {
            this.loginName = loginName
        }?.toModel()
    }
}