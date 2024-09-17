import kotlinx.kover.gradle.plugin.dsl.CoverageUnit

plugins {
    alias(libs.plugins.kotlin.jvm)
    id("maven-publish")
    alias(libs.plugins.kover)
    alias(libs.plugins.detekt)
}


allprojects {
    group = "dev.usbharu"
    version = "0.0.2-SNAPSHOT"


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
        plugin(rootProject.libs.plugins.kover.get().pluginId)
    }
    kotlin {
        jvmToolchain(21)
    }

    dependencies {
        implementation("org.slf4j:slf4j-api:2.0.15")
        testImplementation("org.junit.jupiter:junit-jupiter:5.10.3")


    }
    project.gradle.taskGraph.whenReady {
        if (this.hasTask(":koverGenerateArtifact")) {
            val task = this.allTasks.find { println(it.name);it.name == "test" }
            val verificationTask = task as VerificationTask
            verificationTask.ignoreFailures = true
        }
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

dependencies {
    kover(project(":owl-broker"))
    kover(project(":owl-broker:owl-broker-mongodb"))
    kover(project(":owl-common"))
    kover(project(":owl-common:owl-common-serialize-jackson"))
    kover(project(":owl-consumer"))
    kover(project(":owl-producer"))
    kover(project(":owl-producer:owl-producer-api"))
    kover(project(":owl-producer:owl-producer-default"))
    kover(project(":owl-producer:owl-producer-embedded"))
}



project.gradle.taskGraph.whenReady {
    if (this.hasTask(":koverGenerateArtifact")) {
        val task = this.allTasks.find { it.name == "test" }
        val verificationTask = task as VerificationTask
        verificationTask.ignoreFailures = true
    }
}

kover {
    currentProject {
        sources {
            excludedSourceSets.addAll("grpc", "grpckt")
        }
    }
    reports {
        verify {
            rule {
                bound {
                    minValue = 50
                    coverageUnits = CoverageUnit.INSTRUCTION
                }
            }
        }
        total {
            xml {
                title = "Hideout Core"
                xmlFile = file("$buildDir/reports/kover/hideout-core.xml")
            }
            filters {
                excludes {
                    packages("dev.usbharu.owl.generated")
                }
            }
        }


    }
}
