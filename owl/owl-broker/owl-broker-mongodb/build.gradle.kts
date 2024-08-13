plugins {
    application
    alias(libs.plugins.kotlin.jvm)
}

group = "dev.usbharu"

repositories {
    mavenCentral()
}

dependencies {
    implementation(libs.mongodb.kotlin.coroutine)
    implementation(project(":owl-broker"))
    implementation(project(":owl-common"))
    implementation(platform(libs.koin.bom))
    implementation(libs.koin.core)
}

tasks.test {
    useJUnitPlatform()
}
kotlin {
    jvmToolchain(21)
}

application {
    mainClass = "dev.usbharu.owl.broker.MainKt"
}