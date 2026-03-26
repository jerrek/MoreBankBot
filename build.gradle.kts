plugins {
    kotlin("jvm") version "1.9.0"
    id("org.springframework.boot") version "3.1.0"
    id("io.spring.dependency-management") version "1.1.0"
    kotlin("plugin.serialization") version "1.9.0"
}

group = "ru.nox.sisabot"
version = "0.0.1"
java.sourceCompatibility = JavaVersion.VERSION_17

repositories {
    gradlePluginPortal()
    mavenCentral()
}

dependencies {

    implementation("org.springframework.boot:spring-boot-starter")
    implementation("org.springframework.boot:spring-boot-starter-web")

    // Telegram API
    implementation("org.telegram:telegrambots-spring-boot-starter:6.8.0")

    // Kotlin Telegram Bot (InsanusMokrassar)
    implementation("dev.inmo:tgbotapi:23.1.0")

    // Coroutines
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.7.3")

    // Ktor HTTP Client
    implementation("io.ktor:ktor-client-core:2.3.10")
    implementation("io.ktor:ktor-client-cio:2.3.10")
    implementation("io.ktor:ktor-client-content-negotiation:2.3.10")
    implementation("io.ktor:ktor-serialization-kotlinx-json:2.3.10")

//  Google
    implementation("com.google.api-client:google-api-client:2.2.0")
    implementation("com.google.oauth-client:google-oauth-client-jetty:1.34.1")
    implementation("com.google.apis:google-api-services-sheets:v4-rev20250513-2.0.0")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")

    // Kotlinx Serialization
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.3")

    // Kotlin Coroutines
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.8.0")

    // Логирование (опционально)
    implementation("org.slf4j:slf4j-simple:2.0.9")

    // Тесты (опционально)
    testImplementation("org.jetbrains.kotlin:kotlin-test")
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.8.0")

    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.jetbrains.kotlin:kotlin-test")
    testImplementation("org.junit.jupiter:junit-jupiter:5.10.0")
    testImplementation("io.mockk:mockk:1.13.10")
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.7.3")




//    kibana
    // HTTP клиент
    implementation("com.squareup.okhttp3:okhttp:4.12.0")

    // JSON парсер
    implementation("com.google.code.gson:gson:2.10.1")

    // Логирование (опционально, но полезно)
    implementation("org.slf4j:slf4j-simple:2.0.9")
}

tasks.test {
    useJUnitPlatform()
}