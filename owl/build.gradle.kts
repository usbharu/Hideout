plugins {
    alias(libs.plugins.kotlin.jvm)
    id("maven-publish")
}


allprojects {
    group = "dev.usbharu"
    version = "0.0.1"


    repositories {
        mavenCentral()
    }
}

tasks {
    create("publishMavenPublicationToMavenLocal") {
        subprojects.forEach { dependsOn("${it.path}:publishMavenPublicationToMavenLocal") }
    }
    create("publishMavenPublicationToGiteaRepository") {
        subprojects.forEach { dependsOn("${it.path}:publishMavenPublicationToGiteaRepository") }
    }
}

subprojects {
    apply {
        plugin("org.jetbrains.kotlin.jvm")
        plugin("maven-publish")
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

    publishing {
        repositories {
            maven {
                name = "Gitea"
                url = uri("https://git.usbharu.dev/api/packages/usbharu/maven")

                credentials(HttpHeaderCredentials::class) {
                    name = "Authorization"
                    value = "token " + (project.findProperty("gpr.gitea") as String? ?: System.getenv("GITEA"))
                }

                authentication {
                    create<HttpHeaderAuthentication>("header")
                }
            }
        }

        publications {
            register<MavenPublication>("maven") {
                groupId = "dev.usbharu"
                artifactId = project.name
                version = project.version.toString()
                from(components["kotlin"])
            }
        }
    }
}