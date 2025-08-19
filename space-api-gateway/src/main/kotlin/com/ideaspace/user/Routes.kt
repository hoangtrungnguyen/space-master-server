package com.ideaspace.user

import com.ideaspace.config.AuthPrincipal
import com.ideaspace.config.UserInfo
import io.ktor.http.*
import io.ktor.server.auth.authenticate
import io.ktor.server.auth.principal
import io.ktor.server.plugins.di.*
import io.ktor.server.request.*
import io.ktor.server.response.respond
import io.ktor.server.routing.*

fun Route.userManagementRoutes() {
    post("/api/users/register") {
        val request = call.receive<RegisterNameRequest>()
        val command = RegisterNameCommand(request)
        val user = command.execute(application.dependencies)
        call.respond(HttpStatusCode.OK, UserInfo(
            id = user.id,
            loginName = user.loginName,
            fullName = user.fullName
        ))
    }
    authenticate("jwt-auth") {
        get("/api/users/profile") {
            val principal = call.principal<AuthPrincipal>()!!
            val user = principal.user
            call.respond(HttpStatusCode.OK, UserInfo(
                id = user.id,
                loginName = user.loginName,
                fullName = user.fullName
            ))
        }
    }
}
