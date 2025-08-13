package com.ideaspace.user

import com.ideaspace.core.models.User
import com.ideaspace.core.repository.UserRepo
import io.ktor.server.plugins.di.DependencyRegistry
import kotlinx.serialization.Serializable

class RegisterNameCommand(
    val request: RegisterNameRequest
){
    suspend fun execute(dependencies: DependencyRegistry): User {
        val userRepo = dependencies.resolve<UserRepo>()
        return userRepo.create(request.loginName, request.loginName)
    }
}

@Serializable
data class RegisterNameRequest(
    val loginName: String,
)

