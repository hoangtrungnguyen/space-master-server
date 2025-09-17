package com.ideaspace.user

import com.ideaspace.config.*
import io.ktor.http.*
import io.ktor.server.auth.*
import io.ktor.server.plugins.di.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.sessions.*
import java.time.Instant
import java.time.temporal.ChronoUnit

fun Route.userManagementRoutes() {
    post("/api/users/logout") {
        call.sessions.clear(accessTokenName)
        call.respond(HttpStatusCode.OK, "Successfully logged out")
    }

    get("/api/user/verify-session") {
        // Access the cookie value directly
        val myCookie = call.request.cookies[accessTokenName]
        if (myCookie != null) {
            call.respondText(status = HttpStatusCode.OK, text = "Successfully verified session")
        } else {
            call.respondText(status = HttpStatusCode.Unauthorized, text = "Unauthorized")
        }
    }

    post("/api/users/sign-up") {
        val request = call.receive<RegisterNameRequest>()
        val command = RegisterNameCommand(request)
        try {
            val user = command.execute(application.dependencies)

            val expiresAt = Instant.now().plus(7, ChronoUnit.DAYS)
            println("Successfully generated JWT token for user: ${user.loginName} (ID: ${user.id})")

            // Generate JWT token
            val token = call.generateJwtToken(user)

            call.sessions.set(
                accessTokenName, token
            )

            call.respond(
                HttpStatusCode.OK,
                LoginResponse(
                    user = UserInfo(
                        id = user.id,
                        loginName = user.loginName,
                        fullName = user.fullName
                    ),
                    expiresAt = expiresAt.toString()
                )
            )
        } catch (e: Exception) {
            e.printStackTrace()
            call.respond(
                status = HttpStatusCode.BadRequest,
                message = e.message ?: "Unknown error"
            )
        }
    }
    authenticate("auth-session") {
        get("/api/users/profile") {
            val principal = call.principal<AuthPrincipal>()!!
            val user = principal.user
            call.respond(
                HttpStatusCode.OK, UserInfo(
                    id = user.id,
                    loginName = user.loginName,
                    fullName = user.fullName
                )
            )
        }
    }
}
