val postgres_version: String by project
val h2_version: String by project
val logback_version: String by project
val ktor_version: String by project
val confluent_version: String by project

plugins {
    kotlin("jvm") version "2.1.10"
    id("org.jetbrains.kotlin.plugin.serialization") version "2.1.10" apply true
}

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
    implementation("io.ktor:ktor-server-core")

    //---- KAFKA ----
    implementation("io.github.flaxoos:ktor-server-kafka:2.2.1")
    implementation("io.confluent:kafka-json-serializer:${confluent_version}")

    //---- DATABASE ----
    implementation("io.r2dbc:r2dbc-h2:1.0.0.RELEASE")
    implementation("org.postgresql:r2dbc-postgresql:1.0.7.RELEASE")

    //---- LOG ----
    implementation("ch.qos.logback:logback-classic:$logback_version")
    implementation("io.ktor:ktor-serialization-jackson:3.2.2")
    implementation("org.slf4j:slf4j-nop:1.7.30")


    // --- Serialize ---
    implementation("io.ktor:ktor-serialization-kotlinx-json")

    // --- JDBC and R2DBC ---
    implementation("org.jetbrains.exposed:exposed-core:1.0.0-beta-5")
    implementation("org.jetbrains.exposed:exposed-r2dbc:1.0.0-beta-5")
    implementation("org.jetbrains.exposed:exposed-dao:1.0.0-beta-5")
    implementation("org.jetbrains.exposed:exposed-kotlin-datetime:1.0.0-beta-5")

    implementation("org.jetbrains.exposed:exposed-jdbc:1.0.0-beta-5")

    implementation("org.jetbrains.exposed:exposed-json:1.0.0-beta-5")
    implementation("org.postgresql:postgresql:$postgres_version")
    implementation("com.h2database:h2:2.2.224")

    // REDIS
    implementation("io.lettuce:lettuce-core:6.8.0.RELEASE")

    testImplementation(kotlin("test"))
}

tasks.test {
    useJUnitPlatform()
}
kotlin {
    jvmToolchain(21)
}