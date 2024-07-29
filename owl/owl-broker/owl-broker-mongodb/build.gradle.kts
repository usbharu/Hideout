plugins {
    application
    kotlin("jvm")
}

group = "dev.usbharu"
version = "0.0.1"

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.mongodb:mongodb-driver-kotlin-coroutine:5.1.2")
    implementation(project(":owl-broker"))
    implementation(project(":owl-common"))
    implementation(platform("io.insert-koin:koin-bom:3.5.6"))
    implementation(platform("io.insert-koin:koin-annotations-bom:1.3.1"))
    implementation("io.insert-koin:koin-core")
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