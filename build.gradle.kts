

plugins {
    kotlin("jvm") version "2.1.10" apply true
    id("io.ktor.plugin") version "3.2.2" apply true
    id("org.jetbrains.kotlin.plugin.serialization") version "2.1.10" apply true
}



allprojects {
    group = "com.space"
    version = "0.0.1"

    repositories {
        mavenCentral()
        maven { url = uri("https://jitpack.io") }
        maven { url = uri("https://packages.confluent.io/maven/") }
    }
}

subprojects{
    apply(plugin = "org.jetbrains.kotlin.jvm")

    dependencies{

//        implementation("io.ktor:ktor-server-core")
//        implementation("io.ktor:ktor-server-swagger")
//        implementation("io.ktor:ktor-server-auth")
//        implementation("dev.hayden:khealth:3.0.2")
//        implementation("io.ktor:ktor-server-content-negotiation")
//        implementation("io.ktor:ktor-serialization-kotlinx-json")
//        implementation("io.github.flaxoos:ktor-server-kafka:2.2.1")
//        implementation("com.ucasoft.ktor:ktor-simple-redis-cache:0.55.3")
//        implementation("io.ktor:ktor-server-websockets")
//        implementation("io.github.flaxoos:ktor-server-rate-limiting:2.2.1")
//        implementation("io.ktor:ktor-server-netty")
//        implementation("ch.qos.logback:logback-classic:$logback_version")
//        implementation("io.ktor:ktor-server-config-yaml")
//        implementation("io.ktor:ktor-server-call-logging")
//        implementation("io.ktor:ktor-server-di")
//        implementation("io.ktor:ktor-server-cors")
//
//        //--- CLIENT ---
//        implementation("io.ktor:ktor-client-cio")
//        implementation("io.ktor:ktor-client-logging")
//        implementation("io.ktor:ktor-client-content-negotiation")
//
//
//
//        implementation("io.insert-koin:koin-ktor:${koin_version}")
//        implementation("io.insert-koin:koin-logger-slf4j:${koin_version}")
//        testImplementation("io.ktor:ktor-server-test-host")
//        testImplementation("org.jetbrains.kotlin:kotlin-test-junit:$kotlin_version")
    }

}

