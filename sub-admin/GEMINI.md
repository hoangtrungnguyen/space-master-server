# Project Overview

This project is a backend service for **Local Store**. This service will be installed on store's computers to support offline mode. All data will be stored locally. In the premium feature, if the user wants to store data remotely, we will provide a mechanism to sync data to the server.

# Building and Running

The project uses Gradle as a build tool.

**To build the project, run:**

```bash
./gradlew build
```

**To run the project, run:**

```bash
./gradlew bootRun
```

**To run the tests, run:**

```bash
./gradlew test
```

# Development Conventions

*   **Language**: Kotlin 1.9+
*   **Framework**: Spring Boot 3.x
*   **Build Tool**: Gradle with Kotlin DSL (`build.gradle.kts`)
*   **Database**: PostgreSQL
*   **ORM**: Spring Data JPA with Hibernate
*   **Sync data**: SymmetricDS
*   **Testing**: JUnit 5 and MockK.

## Architectural Patterns

*   **Layered Architecture**: Follow a standard Controller-Service-Repository pattern.
    *   `Controllers`: Handle HTTP requests and responses. Should be stateless.
    *   `Services`: Contain all business logic.
    *   `Repositories`: Manage data access using Spring Data JPA.
    *   `Commands`: Class files that act as an action from the user and have complex business logic.
*   **Dependency Injection**: Use constructor injection for all Spring components.
*   **DTOs**: Use Data Transfer Objects (DTOs) to transfer data between the controller and service layers. Avoid exposing JPA entities directly in the API.

## Coding Conventions & Style

*   **Null Safety**: Leverage Kotlin's null-safety features strictly. Avoid `!!` operator.
*   **Immutability**: Prefer `val` over `var` and use immutable data classes where possible.
*   **Functional Style**: Use scope functions (`let`, `run`, `apply`, `also`) where it improves readability.
*   **Error Handling**: Use Spring's `@ControllerAdvice` for global exception handling. Service methods should throw specific exceptions that are handled at the controller level.
*   **Logging**: Use SLF4J for logging.
