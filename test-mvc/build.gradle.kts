plugins {
    kotlin("jvm")
    kotlin("plugin.spring") version "1.9.22"
    id("org.springframework.boot") version "3.2.2"
}


apply {
    plugin("io.spring.dependency-management")
}

group = "dev.usbharu"
version = "0.0.1"

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-test")
}

tasks.test {
    useJUnitPlatform()
}
kotlin {
    jvmToolchain(21)
}