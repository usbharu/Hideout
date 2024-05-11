plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.kotlin.spring)
    alias(libs.plugins.spring.boot)
}

apply {
    plugin("io.spring.dependency-management")
}

group = "dev.usbharu"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
    maven {
        url = uri("https://git.usbharu.dev/api/packages/usbharu/maven")
    }
    maven {
        name = "GitHubPackages"
        url = uri("https://maven.pkg.github.com/usbharu/http-signature")
        credentials {

            username = project.findProperty("gpr.user") as String? ?: System.getenv("USERNAME")
            password = project.findProperty("gpr.key") as String? ?: System.getenv("TOKEN")
        }
    }
    maven {
        name = "GitHubPackages2"
        url = uri("https://maven.pkg.github.com/multim-dev/emoji-kt")
        credentials {

            username = project.findProperty("gpr.user") as String? ?: System.getenv("USERNAME")
            password = project.findProperty("gpr.key") as String? ?: System.getenv("TOKEN")
        }
    }
}

dependencies {
    testImplementation(kotlin("test"))
    implementation("dev.usbharu:owl-consumer:0.0.1")
    implementation("dev.usbharu:owl-common:0.0.1")
    implementation("dev.usbharu:owl-common-serialize-jackson:0.0.1")
    implementation("dev.usbharu:hideout-core:0.0.1")
    implementation("dev.usbharu:http-signature:1.0.0")
    implementation("org.springframework.boot:spring-boot-starter")
    implementation("org.jetbrains.kotlin:kotlin-reflect")
    implementation("org.springframework.boot:spring-boot-starter-log4j2")
    implementation(libs.jackson.databind)
    implementation(libs.jackson.module.kotlin)
    implementation(libs.bundles.coroutines)

    testImplementation("org.springframework.boot:spring-boot-starter-test")
}

configurations {
    all {
        exclude("org.springframework.boot", "spring-boot-starter-logging")
        exclude("ch.qos.logback", "logback-classic")
    }
}

tasks.test {
    useJUnitPlatform()
}
kotlin {
    jvmToolchain(21)
}