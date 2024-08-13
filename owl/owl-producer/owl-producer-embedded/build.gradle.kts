plugins {
    alias(libs.plugins.kotlin.jvm)
}

group = "dev.usbharu"

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(kotlin("test"))
    implementation(project(":owl-producer:owl-producer-api"))
    implementation(project(":owl-broker"))
    implementation(platform(libs.koin.bom))
    implementation(libs.koin.core)
    implementation(libs.coroutines.core)
}

tasks.test {
    useJUnitPlatform()
}
kotlin {
    jvmToolchain(21)
}