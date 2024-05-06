import com.github.jk1.license.filter.DependencyFilter
import com.github.jk1.license.filter.LicenseBundleNormalizer
import com.github.jk1.license.importer.DependencyDataImporter
import com.github.jk1.license.importer.XmlReportImporter
import com.github.jk1.license.render.*
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import org.openapitools.generator.gradle.plugin.tasks.GenerateTask

val ktor_version: String by project
val kotlin_version: String by project
val exposed_version: String by project
val h2_version: String by project
val koin_version: String by project
val coroutines_version: String by project
val serialization_version: String by project

plugins {
    kotlin("jvm") version "1.9.23"
    id("io.gitlab.arturbosch.detekt") version "1.23.6"
    id("org.springframework.boot") version "3.2.5"
    kotlin("plugin.spring") version "1.9.23"
    id("org.openapi.generator") version "7.4.0"
    id("org.jetbrains.kotlinx.kover") version "0.7.6"
    id("com.github.jk1.dependency-license-report") version "2.5"

}

apply {
    plugin("io.spring.dependency-management")
}

group = "dev.usbharu"
version = "0.0.1"

sourceSets {
    create("intTest") {
        compileClasspath += sourceSets.main.get().output
        runtimeClasspath += sourceSets.main.get().output
    }
    create("e2eTest") {
        compileClasspath += sourceSets.main.get().output
        runtimeClasspath += sourceSets.main.get().output
    }
}

val intTestImplementation by configurations.getting {
    extendsFrom(configurations.implementation.get())
}
val intTestRuntimeOnly by configurations.getting {
    extendsFrom(configurations.runtimeOnly.get())
}

val e2eTestImplementation by configurations.getting {
    extendsFrom(configurations.implementation.get())
}

val e2eTestRuntimeOnly by configurations.getting {
    extendsFrom(configurations.runtimeOnly.get())
}

val integrationTest = task<Test>("integrationTest") {
    description = "Runs integration tests."
    group = "verification"

    testClassesDirs = sourceSets["intTest"].output.classesDirs
    classpath = sourceSets["intTest"].runtimeClasspath
    shouldRunAfter("test")

    useJUnitPlatform()
}

val e2eTest = task<Test>("e2eTest") {
    description = "Runs e2e tests."
    group = "verification"

    testClassesDirs = sourceSets["e2eTest"].output.classesDirs
    classpath = sourceSets["e2eTest"].runtimeClasspath
    shouldRunAfter("test")

    useJUnitPlatform()
}

tasks.check {
    dependsOn(integrationTest)
    dependsOn(e2eTest)
}

tasks.withType<Test> {
    useJUnitPlatform()
    doFirst {
        jvmArgs = arrayOf(
            "--add-opens", "java.base/java.lang=ALL-UNNAMED",
            "--add-opens", "java.base/java.util=ALL-UNNAMED",
            "--add-opens", "java.naming/javax.naming=ALL-UNNAMED",
        ).toMutableList()
    }
}


tasks.withType<KotlinCompile> {
    kotlinOptions {
        freeCompilerArgs += "-Xjsr305=strict"
    }
    dependsOn("openApiGenerateMastodonCompatibleApi")
    mustRunAfter("openApiGenerateMastodonCompatibleApi")
}


tasks.clean {
    delete += listOf("$rootDir/src/main/resources/static")
}

tasks.create<GenerateTask>("openApiGenerateMastodonCompatibleApi", GenerateTask::class) {
    generatorName.set("kotlin-spring")
    inputSpec.set("$rootDir/src/main/resources/openapi/mastodon.yaml")
    outputDir.set("$buildDir/generated/sources/mastodon")
    apiPackage.set("dev.usbharu.hideout.controller.mastodon.generated")
    modelPackage.set("dev.usbharu.hideout.domain.mastodon.model.generated")
    configOptions.put("interfaceOnly", "true")
    configOptions.put("useSpringBoot3", "true")
    configOptions.put("reactive", "true")
    additionalProperties.put("useTags", "true")

    importMappings.put("org.springframework.core.io.Resource", "org.springframework.web.multipart.MultipartFile")
    typeMappings.put("org.springframework.core.io.Resource", "org.springframework.web.multipart.MultipartFile")
    schemaMappings.put(
        "StatusesRequest",
        "dev.usbharu.hideout.mastodon.interfaces.api.status.StatusesRequest"
    )
    templateDir.set("$rootDir/templates")
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

kotlin {
    target {
        compilations.all {
            kotlinOptions.jvmTarget = JavaVersion.VERSION_21.toString()
        }
    }
}

sourceSets.main {
    kotlin.srcDirs(
        "$buildDir/generated/ksp/main",
        "$buildDir/generated/sources/openapi/src/main/kotlin",
        "$buildDir/generated/sources/mastodon/src/main/kotlin"
    )
}

val os = org.gradle.nativeplatform.platform.internal
    .DefaultNativePlatform.getCurrentOperatingSystem()

dependencies {
    implementation("io.ktor:ktor-serialization-jackson:$ktor_version")
    implementation("org.jetbrains.exposed:exposed-core:$exposed_version")
    implementation("org.jetbrains.exposed:exposed-jdbc:$exposed_version")
    developmentOnly("com.h2database:h2:$h2_version")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-core:$serialization_version")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:$serialization_version")

    implementation("org.springframework.boot:spring-boot-starter-actuator")

    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-security")
    implementation("org.springframework.boot:spring-boot-starter-thymeleaf")
    implementation("org.springframework.boot:spring-boot-starter-oauth2-authorization-server")
    implementation("org.springframework.boot:spring-boot-starter-oauth2-resource-server")
    implementation("org.springframework.boot:spring-boot-starter-log4j2")
    implementation("org.springframework.boot:spring-boot-starter-validation")
    implementation("jakarta.validation:jakarta.validation-api")
    implementation("jakarta.annotation:jakarta.annotation-api:3.0.0")
    implementation("io.swagger.core.v3:swagger-annotations:2.2.21")
    implementation("io.swagger.core.v3:swagger-models:2.2.21")
    implementation("org.jetbrains.exposed:exposed-java-time:$exposed_version")
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    implementation("com.fasterxml.jackson.datatype:jackson-datatype-jsr310")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
    implementation("org.springframework.security:spring-security-oauth2-jose")
    implementation("org.springframework.boot:spring-boot-starter-data-mongodb")
    implementation("org.springframework.boot:spring-boot-starter-data-mongodb-reactive")
    implementation("org.jetbrains.exposed:exposed-spring-boot-starter:$exposed_version")
    implementation("io.trbl:blurhash:1.0.0")
    implementation("software.amazon.awssdk:s3:2.25.45")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:$coroutines_version")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-reactor:$coroutines_version")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-slf4j:$coroutines_version")
    implementation("dev.usbharu:http-signature:1.0.0")

    implementation("org.postgresql:postgresql:42.7.3")
    implementation("com.twelvemonkeys.imageio:imageio-webp:3.10.1")
    implementation("org.apache.tika:tika-core:2.9.2")
    implementation("org.apache.tika:tika-parsers:2.9.2")
    implementation("net.coobird:thumbnailator:0.4.20")
    implementation("org.bytedeco:javacv:1.5.10") {
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
        implementation("org.bytedeco", "ffmpeg", "6.1.1-1.5.10", classifier = "windows-x86_64")
    } else {
        implementation("org.bytedeco", "ffmpeg", "6.1.1-1.5.10", classifier = "linux-x86_64")
    }
    implementation("org.flywaydb:flyway-core")

    implementation("dev.usbharu:emoji-kt:2.0.0")
    implementation("org.jsoup:jsoup:1.17.2")
    implementation("com.googlecode.owasp-java-html-sanitizer:owasp-java-html-sanitizer:20240325.1")

    implementation("io.ktor:ktor-client-logging-jvm:$ktor_version")

    testImplementation("org.jetbrains.kotlin:kotlin-test-junit:$kotlin_version")
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:$coroutines_version")

    implementation("io.ktor:ktor-client-core:$ktor_version")
    implementation("io.ktor:ktor-client-cio:$ktor_version")
    implementation("io.ktor:ktor-client-content-negotiation:$ktor_version")
    testImplementation("io.ktor:ktor-client-mock:$ktor_version")
    testImplementation("com.h2database:h2:$h2_version")

    testImplementation("org.mockito.kotlin:mockito-kotlin:5.2.1")
    testImplementation("org.mockito:mockito-inline:5.2.0")
    testImplementation("nl.jqno.equalsverifier:equalsverifier:3.16.1")
    testImplementation("com.jparams:to-string-verifier:1.4.8")

    implementation("org.drewcarlson:kjob-core:0.6.0")
    implementation("org.drewcarlson:kjob-mongo:0.6.0")

    detektPlugins("io.gitlab.arturbosch.detekt:detekt-formatting:1.23.6")

    intTestImplementation("org.springframework.boot:spring-boot-starter-test")
    intTestImplementation("org.springframework.security:spring-security-test")
    intTestImplementation("org.jetbrains.kotlin:kotlin-test-junit:$kotlin_version")
    intTestImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:$coroutines_version")
    intTestImplementation("org.mockito.kotlin:mockito-kotlin:5.2.1")
    intTestImplementation("com.h2database:h2:$h2_version")

    e2eTestImplementation("org.springframework.boot:spring-boot-starter-test")
    e2eTestImplementation("org.springframework.security:spring-security-test")
    e2eTestImplementation("org.springframework.boot:spring-boot-starter-webflux")
    e2eTestImplementation("org.jsoup:jsoup:1.17.2")
    e2eTestImplementation("com.intuit.karate:karate-junit5:1.4.1")
    e2eTestImplementation("com.h2database:h2:$h2_version")

}

detekt {
    parallel = true
    config = files("detekt.yml")
    buildUponDefaultConfig = true
    basePath = "${rootDir.absolutePath}/src/main/kotlin"
    autoCorrect = true
}

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

    excludeSourceSets {
        names("aot", "e2eTest", "intTest")
    }
}

koverReport {
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

springBoot {
    buildInfo()
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
