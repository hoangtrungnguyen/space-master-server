package com.space.subadmin.authentication

import io.jsonwebtoken.Claims
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.security.Keys
import org.springframework.security.core.Authentication
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.stereotype.Service
import java.util.Date
import java.util.UUID
import javax.crypto.SecretKey

@Service
class AppTokenService {

    // TODO: Externalize this secret. For HS512, the key should be at least 64 bytes long.
    private val secret = "your-secret-key-your-secret-key-your-secret-key-your-secret-key"
    private val secretKey: SecretKey by lazy {
        Keys.hmacShaKeyFor(secret.toByteArray())
    }

    private val expiration = 86400000 // 24 hours in milliseconds

    fun generateToken(authentication: Authentication): String {
        val userPrincipal = authentication.principal as UserDetails
        val now = Date()
        val expiryDate = Date(now.time + expiration)
        val extraClaims = mapOf<String, Any>() // Add any other claims you need here
        return Jwts.builder()
            .setSubject(userPrincipal.username)
            .setId(UUID.randomUUID().toString())
            .setIssuedAt(now)
            .setExpiration(expiryDate)
            .signWith(secretKey)
            .addClaims(extraClaims)
            .compact()
    }


    fun validateToken(token: String, userDetails: UserDetails): Boolean {
        return try {
            val username = getSubjectFromToken(token)
            username == userDetails.username && !isTokenExpired(token)
        } catch (e: Exception) {
            // If validation fails for any reason (e.g., parsing, signature), return false.
            false
        }
    }

    private fun isTokenExpired(token: String): Boolean {
        // This will throw an exception if the token is already expired, which is handled by the parser.
        val expirationDate = getExpirationDateFromToken(token)
        return expirationDate.before(Date())
    }


    fun getExpirationDateFromToken(token: String): Date =
        getClaim(token, Claims::getExpiration)
            ?: throw IllegalStateException("Expiration date claim not found in token")

    fun getJtiFromToken(token: String): String? =
        getClaim(token, Claims::getId)

    /**
     * A generic function to extract a specific claim from the token.
     *
     * @param token The JWT string.
     * @param claimsResolver A function that takes a Claims object and returns the desired claim value.
     * @return The claim value, or null if parsing fails.
     */
    private fun <T> getClaim(token: String, claimsResolver: (Claims) -> T): T? {
        return try {
            val claims = getAllClaims(token)
            claimsResolver(claims)
        } catch (e: Exception) {
            // Log the exception in a real application
            println("Failed to parse JWT claims: ${e.message}")
            null
        }
    }

    private fun getAllClaims(token: String): Claims {
        return Jwts.parserBuilder()
            .setSigningKey(secretKey)
            .build()
            .parseClaimsJws(token)
            .body
    }

    fun getSubjectFromToken(token: String): String? {
        return getClaim(token, Claims::getSubject)
    }
}
