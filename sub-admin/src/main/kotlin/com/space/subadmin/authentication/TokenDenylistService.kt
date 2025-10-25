package com.space.subadmin.authentication

import com.github.benmanes.caffeine.cache.Caffeine
import com.github.benmanes.caffeine.cache.Expiry
import org.springframework.stereotype.Service
import java.time.Duration

/**
 * Manages a denylist of JWTs that have been invalidated (e.g., due to logout).
 * This implementation uses an in-memory Caffeine cache, which is a high-performance
 * caching library perfect for this use case. Tokens are stored until they would
 * have naturally expired.
 */
@Service
class TokenDenylistService(private val tokenService: AppTokenService) {

    // A cache to store the JTI (JWT ID) of denylisted tokens.
    // We use a cache so that tokens are automatically evicted after they expire.
    private val denylistCache = Caffeine.newBuilder()
        // Configure a variable expiration time for each entry.
        .expireAfter(object : Expiry<String, Long> {
            // The 'value' here is the token's original expiration timestamp.
            override fun expireAfterCreate(key: String, value: Long, currentTime: Long): Long {
                // Calculate the remaining time until the token expires, in nanoseconds.
                val remainingTime = value - System.currentTimeMillis()
                return Duration.ofMillis(remainingTime.coerceAtLeast(0)).toNanos()
            }
            // These are not needed for our use case, but must be implemented.
            override fun expireAfterUpdate(key: String, value: Long, currentTime: Long, currentDuration: Long) = currentDuration
            override fun expireAfterRead(key: String, value: Long, currentTime: Long, currentDuration: Long) = currentDuration
        })
        .build<String, Long>()

    fun addToDenylist(token: String) {
        val jti = tokenService.getJtiFromToken(token)
        if (jti != null) {
            val expirationTime = tokenService.getExpirationDateFromToken(token).time
            // Store the JTI with its original expiration time. The Expiry implementation will use this.
            denylistCache.put(jti, expirationTime)
        }
    }

    fun isDenylisted(token: String): Boolean {
        val jti = tokenService.getJtiFromToken(token)
        return denylistCache.getIfPresent(jti) != null
    }
}