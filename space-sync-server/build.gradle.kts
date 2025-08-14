val h2_version: String by project
val koin_version: String by project
val kotlin_version: String by project
val logback_version: String by project
val postgres_version: String by project
val ktor_version: String by project
val confluent_version: String by project

plugins {
    kotlin("jvm")
    id("application")
    id("org.jetbrains.kotlin.plugin.serialization") version "2.1.10" apply true
}


application {
    mainClass = "io.ktor.server.netty.EngineMain"
}

repositories {
    mavenCentral()
    maven { url = uri("https://jitpack.io") }
    maven { url = uri("https://packages.confluent.io/maven/") }
}

dependencies {
    implementation(platform ("io.ktor:ktor-bom:${ktor_version}"))
    implementation(project(":space-core"))

    //---- KTOR client ----
    implementation("io.ktor:ktor-client-core")
    implementation("io.ktor:ktor-client-content-negotiation")

    //---- KTOR sever ----
    implementation("io.ktor:ktor-server-call-logging")
    implementation("io.ktor:ktor-server-core")
    implementation("io.ktor:ktor-server-netty")
    implementation("io.ktor:ktor-server-swagger")
    implementation("io.ktor:ktor-server-host-common")
    implementation("io.ktor:ktor-server-status-pages")
    implementation("io.ktor:ktor-server-config-yaml")
    implementation("dev.hayden:khealth:3.0.2")
    implementation("io.ktor:ktor-server-content-negotiation")
    implementation("io.ktor:ktor-serialization-kotlinx-json")
    implementation("io.ktor:ktor-server-di")

    //---- UTILS ----

    //datetime
    implementation("org.jetbrains.kotlinx:kotlinx-datetime:0.6.2") // Use the latest version


    //---- KAFKA ----
    implementation("io.github.flaxoos:ktor-server-kafka:2.2.1")
    implementation("io.confluent:kafka-json-serializer:${confluent_version}")

    // ---- Scheduling ----
//    implementation("io.github.flaxoos:ktor-server-task-scheduling-core:2.2.1")
//    implementation("io.github.flaxoos:ktor-server-task-scheduling-redis:2.2.1")
//    implementation("io.github.flaxoos:ktor-server-task-scheduling-mongodb:2.2.1")
//    implementation("io.github.flaxoos:ktor-server-task-scheduling-jdbc:2.2.1")


    //---- DATABASE ----
    implementation("org.jetbrains.exposed:exposed-r2dbc:1.0.0-beta-5")
    implementation("org.jetbrains.exposed:exposed-jdbc:1.0.0-beta-5")
    implementation("org.jetbrains.exposed:exposed-dao:1.0.0-beta-5")

    // REDIS
    implementation("redis.clients:jedis:5.1.3")

    //LOGGING
    implementation("ch.qos.logback:logback-classic:$logback_version")
    implementation("io.ktor:ktor-serialization-jackson:3.2.2")


    // SERIALIZE
    implementation("io.ktor:ktor-serialization-kotlinx-json")

    //TESTING
    testImplementation("io.ktor:ktor-server-test-host")
    testImplementation("org.jetbrains.kotlin:kotlin-test-junit:$kotlin_version")
}


kotlin {
    jvmToolchain(21)
}