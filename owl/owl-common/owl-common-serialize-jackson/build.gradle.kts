plugins {
    alias(libs.plugins.kotlin.jvm)
}

group = "dev.usbharu"
version = "0.0.1"

repositories {
    mavenCentral()
}

dependencies {
    implementation(project(":owl-common"))
    testImplementation(kotlin("test"))
    implementation(libs.bundles.jackson)
}

tasks.test {
    useJUnitPlatform()
}
kotlin {
    jvmToolchain(21)
}