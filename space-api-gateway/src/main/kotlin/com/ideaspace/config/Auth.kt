package com.ideaspace.config

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.ideaspace.core.models.User
import com.ideaspace.core.repository.UserRepo
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.plugins.di.dependencies
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.serialization.Serializable
import java.net.URLDecoder
import java.time.Instant
import java.time.temporal.ChronoUnit
import java.util.*

data class AuthClaims(
    val user: User
)

@Serializable
data class LoginRequest(
    val loginName: String
)

@Serializable
data class LoginResponse(
    val token: String,
    val user: UserInfo,
    val expiresAt: String
)

@Serializable
data class UserInfo(
    val id: Long,
    val loginName: String,
    val fullName: String
)

/**
 * Authenticates a WebSocket connection using JWT token from query parameters
 * and verifies document access permissions
 */
suspend fun ApplicationCall.authenticate(): AuthClaims? {
    // Extract JWT token from query parameters
    val bearer = request.headers["Authorization"]
    val token = bearer?.substringAfter("Bearer ")
    if (token.isNullOrEmpty()) {
        println("WebSocket authentication failed: No token provided")
        return null
    }

    // Decode the token if it's URL-encoded
    val decodedToken = URLDecoder.decode(token, "UTF-8")

    // Get JWT configuration from environment
    val secret = application.environment.config.property("jwt.secret").getString()
    val issuer = application.environment.config.property("jwt.issuer").getString()
    val audience = application.environment.config.property("jwt.audience").getString()

    // Verify and decode JWT token
    val verifier = JWT
        .require(Algorithm.HMAC256(secret))
        .withAudience(audience)
        .withIssuer(issuer)
        .build()

    val decodedJWT = verifier.verify(decodedToken)
    val userId = decodedJWT.getClaim("userId").asLong()

    if (userId == null) {
        println("WebSocket authentication failed: Invalid token claims")
        return null
    }

    // Verify user exists in database
    val userRepo = application.dependencies.resolve<UserRepo>()
    val user = userRepo.findById(userId)
    if (user == null) {
        println("WebSocket authentication failed: User not found or loginName mismatch")
        return null
    }

    return AuthClaims(user)
}

/**
 * Generates a JWT token for the given user
 */
fun ApplicationCall.generateJwtToken(user: User): String {
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
    route("/api") {
        post("/auth/login") {
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
                val userRepo = call.application.dependencies.resolve<UserRepo>()
                val user = userRepo.findByLoginName(loginRequest.loginName)
                
                if (user == null) {
                    call.respond(
                        HttpStatusCode.Unauthorized,
                        ErrorResponse("USER_NOT_FOUND", "User with login name '${loginRequest.loginName}' not found", null, System.currentTimeMillis())
                    )
                    return@post
                }
                
                // Generate JWT token
                val token = call.generateJwtToken(user)
                val expiresAt = Instant.now().plus(7, ChronoUnit.DAYS)
                
                // Create response
                val response = LoginResponse(
                    token = token,
                    user = UserInfo(
                        id = user.id,
                        loginName = user.loginName,
                        fullName = user.fullName
                    ),
                    expiresAt = expiresAt.toString()
                )
                
                println("Successfully generated JWT token for user: ${user.loginName} (ID: ${user.id})")
                call.respond(HttpStatusCode.OK, response)
                
            } catch (e: Exception) {
                println("Login error: ${e.message}")
                call.respond(
                    HttpStatusCode.InternalServerError,
                    ErrorResponse("INTERNAL_ERROR", "An error occurred during login", null, System.currentTimeMillis())
                )
            }
        }
    }
}

