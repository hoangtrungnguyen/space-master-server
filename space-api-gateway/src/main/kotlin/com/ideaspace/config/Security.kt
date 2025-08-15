package com.ideaspace.config

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

data class JWTPrincipal(val userId: Long, val loginName: String)

fun Application.configureSecurity() {
    val secret = environment.config.property("jwt.secret").getString()
    val issuer = environment.config.property("jwt.issuer").getString()
    val audience = environment.config.property("jwt.audience").getString()
    
    authentication {
        jwt("jwt-auth") {
            realm = "Space Master Server"
            verifier(
                JWT
                    .require(Algorithm.HMAC256(secret))
                    .withAudience(audience)
                    .withIssuer(issuer)
                    .build()
            )
            validate { credential ->
                val userId = credential.payload.getClaim("userId").asLong()
                val email = credential.payload.getClaim("email").asString()
                
                if (userId != null && email != null) {
                    // For now, we'll just validate the token structure
                    // In a real application, you would verify against the database
                    JWTPrincipal(userId, email)
                } else null
            }
            challenge { _, _ ->
                call.respond(HttpStatusCode.Unauthorized, "Token is not valid or has expired")
            }
        }
    }
    routing {
        authenticate("jwt-auth") {
            get("/protected/profile") {
                val principal = call.principal<JWTPrincipal>()!!
                call.respondText("Hello user ${principal.loginName} (ID: ${principal.userId})")
            }
        }
    }
}
