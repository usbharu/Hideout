import dev.usbharu.hideout.core.infrastructure.exposedrepository.Instance.version
import kotlinx.kover.gradle.plugin.dsl.CoverageUnit

plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.detekt)
    alias(libs.plugins.kover)
}

group = "dev.usbharu"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(kotlin("test"))
}

tasks.test {
    useJUnitPlatform()
}
kotlin {
    jvmToolchain(21)
}


configurations {
    matching { it.name == "detekt" }.all {
        resolutionStrategy.eachDependency {
            if (requested.group == "org.jetbrains.kotlin") {
                useVersion(io.gitlab.arturbosch.detekt.getSupportedKotlinVersion())
            }
        }
    }
    all {
        exclude("org.apache.logging.log4j", "log4j-slf4j2-impl")
    }
}

tasks {
    withType<io.gitlab.arturbosch.detekt.Detekt> {
        exclude("**/generated/**")
        setSource("src/main/kotlin")
        exclude("build/")
        configureEach {
            exclude("**/org/koin/ksp/generated/**", "**/generated/**")
        }
    }
    withType<io.gitlab.arturbosch.detekt.DetektCreateBaselineTask>() {
        configureEach {
            exclude("**/org/koin/ksp/generated/**", "**/generated/**")
        }
    }
    withType<Test> {
        useJUnitPlatform()
    }
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
                title = "Hideout ActivityPub"
                xmlFile = file("$buildDir/reports/kover/hideout-activitypub.xml")
            }
        }
        filters {
            excludes {
                annotatedBy("org.springframework.context.annotation.Configuration")
                annotatedBy("org.springframework.boot.context.properties.ConfigurationProperties")
                packages(
                    "dev.usbharu.hideout.controller.mastodon.generated",
                    "dev.usbharu.hideout.domain.mastodon.model.generated"
                )
                packages("org.springframework")
                packages("org.jetbrains")
            }
        }

    }
}
