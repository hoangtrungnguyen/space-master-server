package com.ideaspace.core.dao

import com.ideaspace.core.models.User
import org.jetbrains.exposed.v1.core.dao.id.EntityID
import org.jetbrains.exposed.v1.core.dao.id.LongIdTable
import org.jetbrains.exposed.v1.dao.LongEntity
import org.jetbrains.exposed.v1.dao.LongEntityClass

object UserTable : LongIdTable("users") {
    val loginName = varchar("login_name", 255).uniqueIndex()
    val fullName = varchar("full_name", 255)
}

class UserDAO(id: EntityID<Long>) : LongEntity(id) {
    companion object : LongEntityClass<UserDAO>(UserTable)

    var loginName by UserTable.loginName
    var fullName by UserTable.fullName

    fun toModel(): User = User(id.value, loginName, fullName)
}