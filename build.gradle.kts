import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import org.openapitools.generator.gradle.plugin.tasks.GenerateTask
import kotlin.math.max

val ktor_version: String by project
val kotlin_version: String by project
val logback_version: String by project
val exposed_version: String by project
val h2_version: String by project
val koin_version: String by project

plugins {
    kotlin("jvm") version "1.8.21"
    id("org.graalvm.buildtools.native") version "0.9.21"
    id("io.gitlab.arturbosch.detekt") version "1.23.1"
    id("org.springframework.boot") version "3.1.3"
    kotlin("plugin.spring") version "1.8.21"
    id("org.openapi.generator") version "7.0.1"
    id("org.jetbrains.kotlinx.kover") version "0.7.4"
//    id("org.jetbrains.kotlin.plugin.serialization") version "1.8.10"
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
}

val intTestImplementation by configurations.getting {
    extendsFrom(configurations.implementation.get())
}
val intTestRuntimeOnly by configurations.getting {
    extendsFrom(configurations.runtimeOnly.get())
}

val integrationTest = task<Test>("integrationTest") {
    description = "Runs integration tests."
    group = "verification"

    testClassesDirs = sourceSets["intTest"].output.classesDirs
    classpath = sourceSets["intTest"].runtimeClasspath
    shouldRunAfter("test")

    useJUnitPlatform()

    testLogging {
        events("passed")
    }
}

tasks.check { dependsOn(integrationTest) }

tasks.withType<Test> {
    useJUnitPlatform()
    val cpus = Runtime.getRuntime().availableProcessors()
    maxParallelForks = max(1, cpus - 1)
    setForkEvery(4)
    doFirst {
        jvmArgs = arrayOf(
            "--add-opens", "java.base/java.lang=ALL-UNNAMED"
        ).toMutableList()
    }
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompilationTask<*>>().configureEach {
    compilerOptions.languageVersion.set(org.jetbrains.kotlin.gradle.dsl.KotlinVersion.KOTLIN_1_8)
    compilerOptions.apiVersion.set(org.jetbrains.kotlin.gradle.dsl.KotlinVersion.KOTLIN_1_8)
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
}

kotlin {
    target {
        compilations.all {
            kotlinOptions.jvmTarget = JavaVersion.VERSION_17.toString()
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

dependencies {
    implementation("io.ktor:ktor-serialization-jackson:$ktor_version")
    implementation("org.jetbrains.exposed:exposed-core:$exposed_version")
    implementation("org.jetbrains.exposed:exposed-jdbc:$exposed_version")
    implementation("com.h2database:h2:$h2_version")
    implementation("org.xerial:sqlite-jdbc:3.40.1.0")
    implementation("ch.qos.logback:logback-classic:$logback_version")
    implementation("com.auth0:java-jwt:4.4.0")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-core:1.6.0")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.0")

    implementation("org.springframework.boot:spring-boot-starter-actuator")

    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-security")
    implementation("org.springframework.boot:spring-boot-starter-thymeleaf")
    implementation("org.springframework.boot:spring-boot-starter-oauth2-authorization-server")
    implementation("org.springframework.boot:spring-boot-starter-oauth2-resource-server")
    implementation("jakarta.validation:jakarta.validation-api")
    implementation("jakarta.annotation:jakarta.annotation-api:2.1.0")
    compileOnly("io.swagger.core.v3:swagger-annotations:2.2.6")
    implementation("io.swagger.core.v3:swagger-models:2.2.6")
    implementation("org.jetbrains.exposed:exposed-java-time:$exposed_version")
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    implementation("com.fasterxml.jackson.datatype:jackson-datatype-jsr310")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
    implementation("org.springframework.security:spring-security-oauth2-jose")
    implementation("org.springframework.boot:spring-boot-starter-data-mongodb")
    implementation("org.springframework.boot:spring-boot-starter-data-mongodb-reactive")
    implementation("org.jetbrains.exposed:exposed-spring-boot-starter:0.44.0")
    implementation("io.trbl:blurhash:1.0.0")
    implementation("software.amazon.awssdk:s3:2.20.157")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.7.3")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-reactor:1.7.3")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-slf4j:1.7.3")
    implementation("dev.usbharu:http-signature:1.0.0")

    implementation("org.postgresql:postgresql:42.6.0")
    implementation("com.twelvemonkeys.imageio:imageio-webp:3.10.0")
    implementation("org.apache.tika:tika-core:2.9.1")
    implementation("net.coobird:thumbnailator:0.4.20")

    implementation("io.ktor:ktor-client-logging-jvm:$ktor_version")

    testImplementation("org.jetbrains.kotlin:kotlin-test-junit:$kotlin_version")
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.6.4")

    implementation("io.ktor:ktor-client-core:$ktor_version")
    implementation("io.ktor:ktor-client-cio:$ktor_version")
    implementation("io.ktor:ktor-client-content-negotiation:$ktor_version")
    testImplementation("io.ktor:ktor-client-mock:$ktor_version")

    testImplementation("org.mockito.kotlin:mockito-kotlin:4.1.0")
    testImplementation("org.mockito:mockito-inline:5.2.0")
    testImplementation("nl.jqno.equalsverifier:equalsverifier:3.15.3")
    testImplementation("com.jparams:to-string-verifier:1.4.8")

    implementation("org.drewcarlson:kjob-core:0.6.0")
    implementation("org.drewcarlson:kjob-mongo:0.6.0")

    detektPlugins("io.gitlab.arturbosch.detekt:detekt-formatting:1.23.1")

    intTestImplementation("org.springframework.boot:spring-boot-starter-test")
    intTestImplementation("org.springframework.security:spring-security-test")

}

detekt {
    parallel = true
    config = files("detekt.yml")
    buildUponDefaultConfig = true
    basePath = "${rootDir.absolutePath}/src/main/kotlin"
    autoCorrect = true
}

tasks.withType<io.gitlab.arturbosch.detekt.Detekt>() {
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

configurations.matching { it.name == "detekt" }.all {
    resolutionStrategy.eachDependency {
        if (requested.group == "org.jetbrains.kotlin") {
            useVersion("1.9.0")
        }
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
        names("aot")
    }
}

koverReport {
    filters {
        excludes {
            packages(
                "dev.usbharu.hideout.controller.mastodon.generated",
                "dev.usbharu.hideout.domain.mastodon.model.generated"
            )
            packages("org.springframework")
            packages("org.jetbrains")
        }
    }
}
