val koin_version: String by project
val kotlin_version: String by project
val logback_version: String by project
val graphql_version: String by project
val exposed_database: String by project
val i18n_version: String by project
val restAssured_version: String by project
val kotest_version: String by project
val mockkVersion: String by project
val junit_platform_version: String by project
val status_pages: String by project
val junit_version: String by project

plugins {
    kotlin("jvm") version "2.1.0"
    id("io.ktor.plugin") version "3.0.2"
    id("org.jetbrains.kotlin.plugin.serialization") version "2.1.0"
    id("de.comahe.i18n4k") version "0.10.0"
}

group = "com.example"
version = "0.0.1"

application {
    mainClass.set("io.ktor.server.netty.EngineMain")

    val isDevelopment: Boolean = project.ext.has("development")
    applicationDefaultJvmArgs = listOf("-Dio.ktor.development=$isDevelopment")
}

repositories {
    mavenCentral()
    maven ("https://jitpack.io")
}

tasks.test {
    useJUnitPlatform()
}

dependencies {

    //Ktor
    implementation("io.ktor:ktor-server-core-jvm")
    implementation("io.ktor:ktor-server-cors-jvm")
    implementation("io.ktor:ktor-server-content-negotiation-jvm")
    implementation("io.ktor:ktor-serialization-kotlinx-json-jvm")
    implementation("io.ktor:ktor-server-netty-jvm")
    implementation("io.ktor:ktor-server-config-yaml-jvm")
    implementation("io.ktor:ktor-server-status-pages:$status_pages")

    //Koin
    implementation("io.insert-koin:koin-ktor:$koin_version")
    implementation("io.insert-koin:koin-logger-slf4j:$koin_version")

    //Log
    implementation("ch.qos.logback:logback-classic:$logback_version")

    //Database
    implementation("org.jetbrains.exposed:exposed-core:$exposed_database")
    implementation("org.jetbrains.exposed:exposed-dao:$exposed_database")
    runtimeOnly("org.jetbrains.exposed:exposed-jdbc:$exposed_database")

    //GraphQL
    implementation("com.expediagroup:graphql-kotlin-server:$graphql_version")

    //i18n
    implementation("de.comahe.i18n4k:i18n4k-core-jvm:$i18n_version")

    //Tests
    testImplementation("org.junit.jupiter:junit-jupiter:$junit_version")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:$junit_version")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher:$junit_platform_version")

    testImplementation("io.ktor:ktor-server-test-host-jvm")
    testImplementation("io.rest-assured:rest-assured:$restAssured_version")
    testImplementation("io.kotest:kotest-assertions-core:$kotest_version")
    testImplementation("io.mockk:mockk:${mockkVersion}")
}
