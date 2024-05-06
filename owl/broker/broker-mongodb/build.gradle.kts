plugins {
    application
    kotlin("jvm")
    id("com.google.devtools.ksp") version "1.9.23-1.0.20"
}

apply {
    plugin("com.google.devtools.ksp")
}

group = "dev.usbharu"
version = "0.0.1"

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.mongodb:mongodb-driver-kotlin-coroutine:5.0.0")
    implementation(project(":broker"))
    implementation(project(":common"))
    implementation(platform("io.insert-koin:koin-bom:3.5.6"))
    implementation(platform("io.insert-koin:koin-annotations-bom:1.3.1"))
    implementation("io.insert-koin:koin-core")
    compileOnly("io.insert-koin:koin-annotations")
    ksp("io.insert-koin:koin-ksp-compiler:1.3.1")
}

tasks.test {
    useJUnitPlatform()
}
kotlin {
    jvmToolchain(17)
}

application {
    mainClass = "dev.usbharu.owl.broker.MainKt"
}