val postgres_version: String by project
val h2_version: String by project
val logback_version: String by project
val ktor_version: String by project
val confluent_version: String by project

plugins {
    kotlin("jvm") version "2.1.10"
    id("org.jetbrains.kotlin.plugin.serialization") version "2.1.10" apply true
}

group = "org.example"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    implementation(platform ("io.ktor:ktor-bom:${ktor_version}"))

    // ---- KTOR CLIENT ----
    implementation("io.ktor:ktor-client-core")
    implementation("io.ktor:ktor-client-content-negotiation")

    // ---- KTOR SERVER ---
    implementation("io.ktor:ktor-server-di")
    implementation("io.ktor:ktor-server-content-negotiation:3.2.2")

    //---- KAFKA ----
    implementation("io.github.flaxoos:ktor-server-kafka:2.2.1")
    implementation("io.confluent:kafka-json-serializer:${confluent_version}")

    //---- DATABASE ----
    implementation("com.h2database:h2:${h2_version}")
    implementation("org.postgresql:postgresql:${postgres_version}")
    implementation("io.ktor:ktor-server-core")

    //---- LOG ----
    implementation("ch.qos.logback:logback-classic:$logback_version")
    implementation("io.ktor:ktor-serialization-jackson:3.2.2")

    // --- Serialize ---
    implementation("io.ktor:ktor-serialization-kotlinx-json")

    // --- JDBC ---
    implementation("org.jetbrains.exposed:exposed-core:0.61.0")
    implementation("org.jetbrains.exposed:exposed-jdbc:0.61.0")




    testImplementation(kotlin("test"))
}

tasks.test {
    useJUnitPlatform()
}
kotlin {
    jvmToolchain(21)
}