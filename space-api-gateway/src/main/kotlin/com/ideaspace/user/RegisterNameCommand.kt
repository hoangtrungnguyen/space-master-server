package com.ideaspace.user

import com.ideaspace.core.models.User
import com.ideaspace.core.repository.UserRepo
import io.ktor.server.plugins.di.*
import kotlinx.serialization.Serializable

class RegisterNameCommand(
    val request: RegisterNameRequest
){
    suspend fun execute(dependencies: DependencyRegistry): User {
        val userRepo = dependencies.resolve<UserRepo>()
        return userRepo.create(request.loginName, request.fullName)
    }
}

@Serializable
data class RegisterNameRequest(
    val loginName: String,
    val fullName: String,
)

@Serializable
data class UserSession(val name: String, val count: Int, val token: String)
