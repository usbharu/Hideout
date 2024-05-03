plugins {
    kotlin("jvm")
}

group = "dev.usbharu"
version = "0.0.1"

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(kotlin("test"))
    implementation(project(":owl-producer:owl-producer-api"))
    implementation(project(":owl-broker"))
    implementation(platform("io.insert-koin:koin-bom:3.5.3"))
    implementation("io.insert-koin:koin-core")
}

tasks.test {
    useJUnitPlatform()
}
kotlin {
    jvmToolchain(17)
}