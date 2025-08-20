

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
    }

}

project(":space-api-gateway") {
    dependencies {
        project(":space-core")
    }
}