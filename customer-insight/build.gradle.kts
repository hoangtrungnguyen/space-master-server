// Inherit version properties from gradle.properties
val ktor_version: String by project
val kotlin_version: String by project // Read the Kotlin version from gradle.properties

plugins {
    // Apply Spring Boot and Kotlin plugins
    id("org.springframework.boot") version "3.3.4"
    id("io.spring.dependency-management") version "1.1.5"
}

// All dependencies will be grouped under your existing project group
group = "com.space"
version = "0.0.1"

springBoot {
    mainClass.set("com.space.customerinsight.ApplicationKt")
}

repositories {
    mavenCentral()
}

dependencies {
    implementation(platform("io.ktor:ktor-bom:${ktor_version}"))

    // 1. Core Spring Boot Web Starter for creating API endpoints
    implementation("org.springframework.boot:spring-boot-starter-web")

    // 2. Kotlin support for JSON serialization/deserialization
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")

    // 3. Kotlin standard library and reflection, required by Spring
    implementation("org.jetbrains.kotlin:kotlin-reflect")

    // 4. CSV Parsing Library for handling data uploads
    implementation("com.github.doyaaaaaken:kotlin-csv-jvm:1.9.2")


    // 6. Spring Data JPA for database access
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")

    // 7. PostgreSQL Driver (runtime only, as it's not needed for compilation)
    runtimeOnly("org.postgresql:postgresql")

    // --- TESTING DEPENDENCIES ---
    // 7. Spring Boot testing starter (includes JUnit 5, Mockito, etc.)
    testImplementation("org.springframework.boot:spring-boot-starter-test")

    // 8. Testcontainers for running tests against a real PostgreSQL database
    testImplementation("org.testcontainers:postgresql:1.19.8")
    testImplementation("org.testcontainers:junit-jupiter:1.19.8")
}

tasks.withType<Test> {
    useJUnitPlatform()
}

// Configure the project to use JVM 21
kotlin {
    jvmToolchain(21)
}

