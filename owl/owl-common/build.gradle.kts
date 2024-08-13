plugins {
    alias(libs.plugins.kotlin.jvm)
}

group = "dev.usbharu"

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(libs.kotlin.junit)
}

tasks.test {
    useJUnitPlatform()
}
kotlin {
    jvmToolchain(21)
}