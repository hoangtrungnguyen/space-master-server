# Project Context: Idea Space Board - Backend Service

## 1. Project Overview

This project is a backend service for **Local Store**. This service will be install for store's computers to support offline mode. All data will be store local.
The primary users are small to medium-sized tech companies. 

In premium feature, if user want to store data remotely, we will provide a mechanism to sync data to server

## 2. Technology Stack

- **Language**: Kotlin 1.9+
- **Framework**: Spring Boot 3.x
- **Build Tool**: Gradle with Kotlin DSL (`build.gradle.kts`)
- **Database**: PostGres
- **ORM**: Spring Data JPA with Hibernate
- **Sync data**: SymmetricDS
- **Testing**: JUnit 5 and MockK.

## 3. Architectural Patterns

- **Layered Architecture**: Follow a standard Controller-Service-Repository pattern.
    - `Controllers`: Handle HTTP requests and responses. Should be stateless.
    - `Services`: Contain all business logic.
    - `Repositories`: Manage data access using Spring Data JPA.
    - `Commands`: class files that act as an action from user and have complex business logic
- **Dependency Injection**: Use constructor injection for all Spring components.
- **DTOs**: Use Data Transfer Objects (DTOs) to transfer data between the controller and service layers. Avoid exposing JPA entities directly in the API.

## 4. Coding Conventions & Style

- **Null Safety**: Leverage Kotlin's null-safety features strictly. Avoid `!!` operator.
- **Immutability**: Prefer `val` over `var` and use immutable data classes where possible.
- **Functional Style**: Use scope functions (`let`, `run`, `apply`, `also`) where it improves readability.
- **Error Handling**: Use Spring's `@ControllerAdvice` for global exception handling. Service methods should throw specific exceptions that are handled at the controller level.
- **Logging**: Use SLF4J for logging.

## 5. AI Persona

When providing assistance, please act as a **senior backend engineer** with deep expertise in Kotlin and the Spring Boot ecosystem. Prioritize solutions that are idiomatic, scalable, and secure.