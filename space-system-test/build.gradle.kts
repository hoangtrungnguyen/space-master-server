val restAssured : String by project

plugins {
    kotlin("jvm")
}

group = "com.space"
version = "0.0.1"

repositories {
    mavenCentral()
}

dependencies {
    implementation(project(":space-core"))
    implementation(project(":space-sync-server"))
    implementation(project(":space-api-gateway"))

    testImplementation("io.rest-assured:rest-assured:$restAssured")

    testImplementation(kotlin("test"))
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.10.0")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.10.0")
    testImplementation("org.testcontainers:postgresql:1.19.0")
    testImplementation("org.testcontainers:junit-jupiter:1.19.0")

    testImplementation("org.junit.jupiter:junit-jupiter:5.10.0")
    implementation("io.lettuce:lettuce-core:6.3.2.RELEASE")

}

tasks.test {
    useJUnitPlatform()
}
kotlin {
    jvmToolchain(21)
}