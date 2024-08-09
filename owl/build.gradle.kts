plugins {
    alias(libs.plugins.kotlin.jvm)
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
        jvmToolchain(21)
    }

    dependencies {
        implementation("org.slf4j:slf4j-api:2.0.15")
        testImplementation("org.junit.jupiter:junit-jupiter:5.10.3")


    }


    tasks.test {
        useJUnitPlatform()
    }


}