package com.space.subadmin.authentication

import com.fasterxml.jackson.databind.ObjectMapper
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.http.MediaType
import org.springframework.security.core.AuthenticationException
import org.springframework.security.web.AuthenticationEntryPoint
import org.springframework.stereotype.Component

@Component
class ApiAuthenticationEntryPoint(private val objectMapper: ObjectMapper) : AuthenticationEntryPoint {

    override fun commence(
        request: HttpServletRequest,
        response: HttpServletResponse,
        authException: AuthenticationException
    ) {
        response.status = HttpServletResponse.SC_BAD_REQUEST // 400 as requested
        response.contentType = MediaType.APPLICATION_JSON_VALUE

        val errorResponse = mapOf("error" to "Authentication Required", "message" to authException.message)
        response.writer.write(objectMapper.writeValueAsString(errorResponse))
    }
}