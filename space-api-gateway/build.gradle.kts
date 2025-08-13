val h2_version: String by project
val koin_version: String by project
val kotlin_version: String by project
val logback_version: String by project
val postgres_version: String by project
val ktor_version: String by project

plugins {
    kotlin("jvm")
    id("application")
    id("org.jetbrains.kotlin.plugin.serialization") version "2.1.10" apply true
}


application {
    mainClass = "io.ktor.server.netty.EngineMain"
}

repositories{
    mavenCentral()
}

dependencies {
    implementation(platform ("io.ktor:ktor-bom:${ktor_version}"))

    implementation(project(":space-core"))

    // --- SERVER ---
    implementation("io.ktor:ktor-server-core")
    implementation("io.ktor:ktor-server-swagger")
    implementation("io.ktor:ktor-server-auth")
    implementation("io.ktor:ktor-server-status-pages")
    implementation("dev.hayden:khealth:3.0.2")
    implementation("io.ktor:ktor-server-content-negotiation")
    implementation("io.ktor:ktor-serialization-kotlinx-json")
    implementation("io.github.flaxoos:ktor-server-kafka:2.2.1")
    implementation("com.ucasoft.ktor:ktor-simple-redis-cache:0.55.3")
    implementation("io.ktor:ktor-server-websockets")
    implementation("io.github.flaxoos:ktor-server-rate-limiting:2.2.1")
    implementation("io.ktor:ktor-server-netty")
    implementation("ch.qos.logback:logback-classic:$logback_version")
    implementation("io.ktor:ktor-server-config-yaml")
    implementation("io.ktor:ktor-server-call-logging")
    implementation("io.ktor:ktor-server-di")
    implementation("io.ktor:ktor-server-cors")

    //--- CLIENT ---
    implementation("io.ktor:ktor-client-cio")
    implementation("io.ktor:ktor-client-logging")
    implementation("io.ktor:ktor-client-content-negotiation")

    // DATABASE
    implementation("org.jetbrains.exposed:exposed-jdbc:1.0.0-beta-5")
    implementation("org.jetbrains.exposed:exposed-dao:1.0.0-beta-5")

    implementation("io.insert-koin:koin-ktor:${koin_version}")
    implementation("io.insert-koin:koin-logger-slf4j:${koin_version}")
    testImplementation("io.ktor:ktor-server-test-host")
    testImplementation("org.jetbrains.kotlin:kotlin-test-junit:$kotlin_version")
}

kotlin {
    jvmToolchain(21)
}