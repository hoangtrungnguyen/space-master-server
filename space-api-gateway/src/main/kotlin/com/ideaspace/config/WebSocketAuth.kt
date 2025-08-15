package com.ideaspace.config

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.ideaspace.core.models.User
import com.ideaspace.core.repository.UserRepo
import io.ktor.server.application.*
import io.ktor.server.plugins.di.dependencies
import java.net.URLDecoder

data class AuthClaims(
    val user: User
)

/**
 * Authenticates a WebSocket connection using JWT token from query parameters
 * and verifies document access permissions
 */
suspend fun ApplicationCall.authenticate(): AuthClaims? {
    // Extract JWT token from query parameters
    val token = request.queryParameters["token"]
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

