package com.space.subadmin

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpMethod
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.security.web.SecurityFilterChain
import org.springframework.security.web.authentication.rememberme.JdbcTokenRepositoryImpl
import org.springframework.security.web.authentication.rememberme.PersistentTokenRepository
import javax.sql.DataSource

@Configuration
@EnableWebSecurity
class SecurityConfig(
    private val dataSource: DataSource
) {

    @Bean
    fun passwordEncoder(): PasswordEncoder {
        return BCryptPasswordEncoder()
    }

    @Bean
    fun securityFilterChain(http: HttpSecurity): SecurityFilterChain {
        http
            .formLogin { form ->
                form
                    .permitAll()
            }
            .logout { logout ->
                logout
                    .permitAll()
            }
            .rememberMe { remember ->
                remember.tokenRepository(persistentTokenRepository())
            }.authorizeHttpRequests { authz ->
                authz
                    .requestMatchers(HttpMethod.GET, "/api/v1/product-variants").permitAll()
                    .anyRequest().authenticated()
            }

        return http.build()
    }

    @Bean
    fun persistentTokenRepository(): PersistentTokenRepository {
        val tokenRepository = JdbcTokenRepositoryImpl()
        tokenRepository.setDataSource(dataSource)
        return tokenRepository
    }


}
