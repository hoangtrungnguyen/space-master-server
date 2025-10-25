package com.space.subadmin

import io.swagger.v3.oas.annotations.OpenAPIDefinition
import io.swagger.v3.oas.annotations.enums.SecuritySchemeIn
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType
import io.swagger.v3.oas.annotations.info.Info
import io.swagger.v3.oas.annotations.security.SecurityScheme
import org.springframework.context.annotation.Configuration

//@Configuration
//@OpenAPIDefinition(
//    info = Info(
//        title = "Local Store API",
//        version = "v1",
//        description = "Backend service for Local Store"
//    )
//)
//@SecurityScheme(
//    name = "bearerAuth",
//    description = "JWT auth description",
//    scheme = "bearer",
//    type = SecuritySchemeType.HTTP,
//    bearerFormat = "JWT",
//    `in` = SecuritySchemeIn.HEADER
//)
//class OpenApiConfig