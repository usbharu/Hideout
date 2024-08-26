import com.github.jk1.license.filter.DependencyFilter
import com.github.jk1.license.filter.LicenseBundleNormalizer
import com.github.jk1.license.importer.DependencyDataImporter
import com.github.jk1.license.importer.XmlReportImporter
import com.github.jk1.license.render.*
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.detekt)
    alias(libs.plugins.spring.boot)
    alias(libs.plugins.kotlin.spring)
    alias(libs.plugins.kover)
    alias(libs.plugins.license.report)
}

apply {
    plugin("io.spring.dependency-management")
}

group = "dev.usbharu"
version = "0.0.1"


tasks.withType<Test> {
    useJUnitPlatform()
    doFirst {
        jvmArgs = arrayOf(
            "--add-opens", "java.base/java.lang=ALL-UNNAMED",
            "--add-opens", "java.base/java.util=ALL-UNNAMED",
            "--add-opens", "java.naming/javax.naming=ALL-UNNAMED",
            "--add-opens", "java.base/java.util.concurrent.locks=ALL-UNNAMED"
        ).toMutableList()
    }
}

kotlin {
    jvmToolchain(21)
    compilerOptions {
        freeCompilerArgs.add("-Xjsr305=strict")
        jvmTarget = JvmTarget.JVM_21
    }
}


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


val os = org.gradle.nativeplatform.platform.internal
    .DefaultNativePlatform.getCurrentOperatingSystem()

dependencies {
    developmentOnly(libs.h2db)
    developmentOnly("org.springframework.boot:spring-boot-devtools")
    detektPlugins(libs.detekt.formatting)

    implementation(libs.bundles.exposed)
    implementation(libs.bundles.coroutines)
    implementation(libs.bundles.ktor.client)
    implementation(libs.bundles.apache.tika)
    implementation(libs.bundles.openapi)
    implementation(libs.bundles.owl.producer)
    implementation(libs.bundles.owl.broker)
    implementation(libs.bundles.spring.boot.oauth2)
    implementation(libs.bundles.spring.boot.data.mongodb)
    implementation("org.springframework.boot:spring-boot-starter-actuator")
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-security")
    implementation("org.springframework.boot:spring-boot-starter-thymeleaf")
    implementation("org.springframework.boot:spring-boot-starter-log4j2")
    implementation("org.springframework.boot:spring-boot-starter-validation")
    annotationProcessor("org.springframework:spring-context-indexer")

    implementation(libs.blurhash)
    implementation(libs.aws.s3)
    implementation(libs.jsoup)
    implementation(libs.owasp.java.html.sanitizer)
    implementation(libs.postgresql)
    implementation(libs.imageio.webp)
    implementation(libs.thumbnailator)
    implementation(libs.flyway.core)
    runtimeOnly(libs.flyway.postgresql)

    implementation(libs.owl.common.serialize.jackson)

    implementation(libs.javacv) {
        exclude(module = "opencv")
        exclude(module = "flycapture")
        exclude(module = "artoolkitplus")
        exclude(module = "libdc1394")
        exclude(module = "librealsense")
        exclude(module = "librealsense2")
        exclude(module = "tesseract")
        exclude(module = "libfreenect")
        exclude(module = "libfreenect2")
    }
    if (os.isWindows) {
        implementation(variantOf(libs.javacv.ffmpeg) { classifier("windows-x86_64") })
    } else {
        implementation(variantOf(libs.javacv.ffmpeg) { classifier("linux-x86_64") })
    }

    implementation(libs.http.signature)
    implementation(libs.emoji.kt)



    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation(libs.kotlin.junit)
    testImplementation(libs.coroutines.test)
    testImplementation(libs.ktor.client.mock)
    testImplementation(libs.h2db)
    testImplementation(libs.mockito.kotlin)

}

detekt {
    parallel = true
    config.setFrom(files("../detekt.yml"))
    buildUponDefaultConfig = true
    basePath = "${rootDir.absolutePath}/src/main/kotlin"
    autoCorrect = true
}

configurations.matching { it.name == "detekt" }.all {
    resolutionStrategy.eachDependency {
        if (requested.group == "org.jetbrains.kotlin") {
            useVersion(io.gitlab.arturbosch.detekt.getSupportedKotlinVersion())
        }
    }
}

//tasks{
//    bootRun {
//        sourceResources(sourceSets.main.get())
//    }
//}

tasks.withType<io.gitlab.arturbosch.detekt.Detekt> {
    exclude("**/generated/**")
    doFirst {

    }
    setSource("src/main/kotlin")
    exclude("build/")
}

tasks.withType<io.gitlab.arturbosch.detekt.Detekt>().configureEach {
    exclude("**/org/koin/ksp/generated/**", "**/generated/**")
}

tasks.withType<io.gitlab.arturbosch.detekt.DetektCreateBaselineTask>().configureEach {
    exclude("**/org/koin/ksp/generated/**", "**/generated/**")
}

configurations {
    all {
        exclude("org.springframework.boot", "spring-boot-starter-logging")
        exclude("ch.qos.logback", "logback-classic")
    }
}

project.gradle.taskGraph.whenReady {
    println(this.allTasks)
    this.allTasks.map { println(it.name) }
    if (this.hasTask(":koverGenerateArtifact")) {
        println("has task")
        val task = this.allTasks.find { it.name == "test" }
        val verificationTask = task as VerificationTask
        verificationTask.ignoreFailures = true
    }
}

kover {
    currentProject {
        sources {
            excludedSourceSets.addAll(
                "aot", "e2eTest", "intTest"
            )

        }
    }

    reports {
        filters {
            excludes {
                packages(
                    "dev.usbharu.hideout.activitypub.domain.exception",
                    "dev.usbharu.hideout.core.domain.exception",
                    "dev.usbharu.hideout.core.domain.exception.media",
                    "dev.usbharu.hideout.core.domain.exception.resource",
                    "dev.usbharu.hideout.core.domain.exception.resource.local"
                )
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

springBoot {
    buildInfo {

    }
}

licenseReport {
    excludeOwnGroup = true

    importers = arrayOf<DependencyDataImporter>(XmlReportImporter("hideout", File("$projectDir/license-list.xml")))
    renderers = arrayOf<ReportRenderer>(
        InventoryHtmlReportRenderer(),
        CsvReportRenderer(),
        JsonReportRenderer(),
        XmlReportRenderer()
    )
    filters = arrayOf<DependencyFilter>(LicenseBundleNormalizer("$projectDir/license-normalizer-bundle.json", true))
    allowedLicensesFile = File("$projectDir/allowed-licenses.json")
    configurations = arrayOf("productionRuntimeClasspath")
}