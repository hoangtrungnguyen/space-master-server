package com.space.subadmin.authentication

import com.fasterxml.jackson.databind.ObjectMapper
import io.jsonwebtoken.JwtException
import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.http.MediaType
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.AuthenticationException
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter

@Component
class JwtAuthFilter(
    private val tokenService: AppTokenService,
    private val authService: AuthService,
    private val objectMapper: ObjectMapper,
    private val tokenDenylistService: TokenDenylistService
) : OncePerRequestFilter() {

    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain
    ) {
        try {
            val authHeader = request.getHeader("Authorization")

            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                filterChain.doFilter(request, response)
                return
            }

            val token = authHeader.substring(7)

            if (tokenDenylistService.isDenylisted(token)) {
                throw JwtException("Token has been invalidated by logout.")
            }

            val username = tokenService.getSubjectFromToken(token)

            if (username != null && SecurityContextHolder.getContext().authentication == null) {
                val userDetails = authService.loadUserByUsername(username)
                if (!tokenService.validateToken(token, userDetails)) {
                    // If the token is present but invalid, throw an exception to be caught below.
                    // This prevents the request from falling through as "anonymous".
                    throw JwtException("Invalid or expired JWT token.")
                }

                val authToken = UsernamePasswordAuthenticationToken(
                    userDetails, null, userDetails.authorities
                )
                authToken.details = WebAuthenticationDetailsSource().buildDetails(request)
                SecurityContextHolder.getContext().authentication = authToken
            }
            filterChain.doFilter(request, response)
        } catch (ex: Exception) {
            ex.printStackTrace()
            // Catch exceptions related to token validation (e.g., ExpiredJwtException, SignatureException)
            // or user loading issues, and send a clear 401 error.
            handleAuthException(response, "Invalid Token", ex)
        }
    }

    private fun handleAuthException(response: HttpServletResponse, message: String, exception: Exception) {
        response.status = HttpServletResponse.SC_UNAUTHORIZED
        response.contentType = MediaType.APPLICATION_JSON_VALUE

        val errorResponse = mapOf("error" to message, "detail" to (exception.message ?: "Authentication failed"))
        response.writer.write(objectMapper.writeValueAsString(errorResponse))
    }
}
