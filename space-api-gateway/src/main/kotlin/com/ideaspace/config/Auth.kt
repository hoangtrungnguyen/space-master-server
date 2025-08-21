package com.ideaspace.config

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.ideaspace.core.models.User
import com.ideaspace.core.repository.UserRepo
import com.ideaspace.user.UserSession
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.plugins.di.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.sessions.*
import kotlinx.serialization.Serializable
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.time.Instant
import java.time.temporal.ChronoUnit
import java.util.*

val logger: Logger = LoggerFactory.getLogger("com.ideaspace.config.Auth")

data class AuthPrincipal(
    val user: User
)

@Serializable
data class LoginRequest(
    val loginName: String
)

@Serializable
data class LoginResponse(
    val user: UserInfo,
    val expiresAt: String
)

@Serializable
data class UserInfo(
    val id: Long,
    val loginName: String,
    val fullName: String
)


const val accessTokenName: String = "access_token"
/**
 * Generates a JWT token for the given user
 */
private fun ApplicationCall.generateJwtToken(user: User): String {
    val secret = application.environment.config.property("jwt.secret").getString()
    val issuer = application.environment.config.property("jwt.issuer").getString()
    val audience = application.environment.config.property("jwt.audience").getString()

    val expiresAt = Instant.now().plus(7, ChronoUnit.DAYS)

    return JWT.create()
        .withAudience(audience)
        .withIssuer(issuer)
        .withClaim("userId", user.id)
        .withClaim("loginName", user.loginName)
        .withExpiresAt(Date.from(expiresAt))
        .withIssuedAt(Date.from(Instant.now()))
        .sign(Algorithm.HMAC256(secret))
}

/**
 * Configures authentication routes
 */
fun Route.authRoutes() {

    post("/api/users/login") {
        try {
            val loginRequest = call.receive<LoginRequest>()

            // Validate input
            if (loginRequest.loginName.isBlank()) {
                call.respond(
                    HttpStatusCode.BadRequest,
                    ErrorResponse("INVALID_INPUT", "Login name cannot be empty", null, System.currentTimeMillis())
                )
                return@post
            }

            // Find user by loginName
            val userRepo = application.dependencies.resolve<UserRepo>()
            val user = userRepo.findByLoginName(loginRequest.loginName)

            if (user == null) {
                call.respond(
                    HttpStatusCode.Unauthorized,
                    ErrorResponse(
                        "USER_NOT_FOUND",
                        "User with login name '${loginRequest.loginName}' not found",
                        null,
                        System.currentTimeMillis()
                    )
                )
                return@post
            }

            val expiresAt = Instant.now().plus(7, ChronoUnit.DAYS)

            // Create response
            val response = LoginResponse(
                user = UserInfo(
                    id = user.id,
                    loginName = user.loginName,
                    fullName = user.fullName
                ),
                expiresAt = expiresAt.toString()
            )

            println("Successfully generated JWT token for user: ${user.loginName} (ID: ${user.id})")

            // Generate JWT token
            val token = call.generateJwtToken(user)
            call.sessions.set(
                accessTokenName, token
            )
            call.respond(HttpStatusCode.OK, response)

        } catch (e: Exception) {
            println("Login error: ${e.message}")
            call.respond(
                HttpStatusCode.InternalServerError,
                ErrorResponse("INTERNAL_ERROR", "An error occurred during login", null, System.currentTimeMillis())
            )
        }
    }


    post("/api/users/logout") {
        call.sessions.clear<UserSession>()
        call.respond(HttpStatusCode.OK, "Successfully logged out")
    }
}

suspend fun Application.configureSecurity() {
    install(Sessions) {
        cookie<String>(accessTokenName) {
            cookie.path = "/"
            cookie.maxAgeInSeconds = 86400
            // IMPORTANT: Allow the cookie to be sent with cross-site requests
            cookie.extensions["SameSite"] = "None"
            // IMPORTANT: 'SameSite=None' requires the cookie to be secure
            cookie.secure = true
            cookie.httpOnly = true
        }
    }

    val secret = environment.config.property("jwt.secret").getString()
    val issuer = environment.config.property("jwt.issuer").getString()
    val audience = environment.config.property("jwt.audience").getString()

    val userRepo = dependencies.resolve<UserRepo>()

    val verifier = JWT
        .require(Algorithm.HMAC256(secret))
        .withAudience(audience)
        .withIssuer(issuer)
        .build()

    authentication {
        session<String>("auth-session") {
            validate { credential ->
                val decodedToken = verifier.verify(credential)
                val userId = decodedToken.getClaim("userId").asLong()
                if (userId == null) {
                    logger.error("Invalid token claims: Missing userId")
                    return@validate null
                }

                val user = userRepo.findById(userId)
                if (user == null) {
                    println("Invalid token claims: User not found")
                    return@validate null
                }
                return@validate AuthPrincipal(user)
            }

            challenge {
                call.respond(HttpStatusCode.Unauthorized, "Token is not valid or has expired")
            }
        }


    }
}
