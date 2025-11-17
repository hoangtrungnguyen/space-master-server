package com.space.subadmin

import com.space.subadmin.authentication.AuthService
import com.space.subadmin.authentication.ApiAuthenticationEntryPoint
import com.space.subadmin.authentication.JwtAuthFilter
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile
import org.springframework.core.annotation.Order
import org.springframework.http.HttpMethod
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.authentication.dao.DaoAuthenticationProvider
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.security.web.SecurityFilterChain
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter
import org.springframework.security.web.authentication.rememberme.JdbcTokenRepositoryImpl
import org.springframework.security.web.authentication.rememberme.PersistentTokenRepository
import javax.sql.DataSource
import org.springframework.security.config.annotation.web.invoke
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder


@Configuration
@EnableWebSecurity
class SecurityConfig(
    private val authService: AuthService,
    private val dataSource: DataSource,
    private val jwtAuthFilter: JwtAuthFilter,
    private val apiAuthenticationEntryPoint: ApiAuthenticationEntryPoint
) {

    @Bean
    fun passwordEncoder(): PasswordEncoder {
        return BCryptPasswordEncoder()
    }

    @Bean
    @Order(1)
    @Profile("prod")
    fun apiSecurityFilterChain(http: HttpSecurity): SecurityFilterChain {
        http {
            // Apply this filter chain only to API endpoints
            securityMatcher("/api/**")
            // For API, we use stateless session management
            sessionManagement {
                sessionCreationPolicy = SessionCreationPolicy.STATELESS
            }

            authorizeHttpRequests {
                // Allow unauthenticated access to the API login endpoint
                authorize("/api/auth/login", permitAll)

                authorize("/api/dev/**", permitAll)
                // Secure all other API endpoints. This was a security vulnerability.
                authorize(anyRequest, authenticated)
            }

            // Disable CSRF for stateless API
            csrf { disable() }

            // Add your custom JWT filter before the standard auth filter
            // This is crucial for your JWT-based authentication to work for API calls.
            addFilterBefore<UsernamePasswordAuthenticationFilter>(jwtAuthFilter)

            // Set the custom authentication provider
            authenticationProvider()
            
            exceptionHandling {
                authenticationEntryPoint = apiAuthenticationEntryPoint
            }
        }

        return http.build()
    }


    @Bean
    @Order(2)
    @Profile("prod")
    fun webSecurityFilterChain(http: HttpSecurity): SecurityFilterChain {
        http {
            authorizeHttpRequests {
                // Allow public access to OpenAPI documentation and Swagger UI
                authorize("/swagger-ui/**", permitAll)
                authorize("/v3/api-docs/**", permitAll)
                authorize("/swagger-ui.html", permitAll)

                // Secure all non-api requests by default
                authorize(anyRequest, authenticated)
            }
            formLogin {
                permitAll()
                // Customize form login if needed, e.g., loginPage = "/login"
                // Spring Boot provides a default login page if not specified
            }
            logout {
                permitAll()
                // Customize logout if needed
            }
            rememberMe {
                tokenRepository = persistentTokenRepository()
                userDetailsService = authService // Explicitly set the UserDetailsService
            }

            // Set the custom authentication provider
            authenticationProvider()
        }
        return http.build()
    }

    @Bean
    fun persistentTokenRepository(): PersistentTokenRepository {
        val tokenRepository = JdbcTokenRepositoryImpl()
        tokenRepository.setDataSource(dataSource)
        return tokenRepository
    }

    @Bean
    fun authenticationProvider(): DaoAuthenticationProvider {
        val authProvider = DaoAuthenticationProvider()
        authProvider.setUserDetailsService(authService)
        authProvider.setPasswordEncoder(passwordEncoder())
        return authProvider
    }

    @Bean
    @Throws(Exception::class)
    fun authenticationManager(authConfig: AuthenticationConfiguration): AuthenticationManager {
        return authConfig.authenticationManager
    }


}
