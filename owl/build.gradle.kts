plugins {
    kotlin("jvm") version "1.9.23"
}


allprojects {
    group = "dev.usbharu"
    version = "0.0.1"


    repositories {
        mavenCentral()
    }
}

subprojects {
    apply {
        plugin("org.jetbrains.kotlin.jvm")
    }
    kotlin {
        jvmToolchain(17)
    }

    dependencies {
        implementation("org.slf4j:slf4j-api:2.0.13")
        testImplementation("org.junit.jupiter:junit-jupiter:5.10.2")


    }


    tasks.test {
        useJUnitPlatform()
    }


}