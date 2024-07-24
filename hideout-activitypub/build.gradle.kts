import dev.usbharu.hideout.core.infrastructure.exposedrepository.Instance.version

plugins {
    kotlin("jvm") version "1.9.25"
}

group = "dev.usbharu"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(kotlin("test"))
}

tasks.test {
    useJUnitPlatform()
}
kotlin {
    jvmToolchain(21)
}