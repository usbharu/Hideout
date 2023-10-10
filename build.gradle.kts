import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import org.openapitools.generator.gradle.plugin.tasks.GenerateTask
import kotlin.math.min

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
//    id("org.jetbrains.kotlin.plugin.serialization") version "1.8.10"
}

apply {
    plugin("io.spring.dependency-management")
}

group = "dev.usbharu"
version = "0.0.1"

tasks.withType<Test> {
    useJUnitPlatform()
    val cpus = Runtime.getRuntime().availableProcessors()
    maxParallelForks = min(1, cpus - 1)
    setForkEvery(4)
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
    additionalProperties.put("useTags", "true")

    importMappings.put("org.springframework.core.io.Resource", "org.springframework.web.multipart.MultipartFile")
    typeMappings.put("org.springframework.core.io.Resource", "org.springframework.web.multipart.MultipartFile")
    schemaMappings.put(
        "StatusesRequest",
        "dev.usbharu.hideout.domain.model.mastodon.StatusesRequest"
    )
    templateDir.set("$rootDir/templates")
    globalProperties.put("debugModels", "true")
    globalProperties.put("debugOpenAPI", "true")
    globalProperties.put("debugOperations", "true")
}

repositories {
    mavenCentral()
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
    implementation("org.jetbrains.exposed:spring-transaction:$exposed_version")
    implementation("org.springframework.data:spring-data-commons")
    implementation("org.springframework.boot:spring-boot-starter-jdbc")
    implementation("org.springframework.boot:spring-boot-starter-data-jdbc")
    testImplementation("org.springframework.boot:spring-boot-test-autoconfigure")
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    implementation("com.fasterxml.jackson.datatype:jackson-datatype-jsr310")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
    implementation("org.springframework.security:spring-security-oauth2-jose")
    implementation("org.springframework.boot:spring-boot-starter-data-mongodb")
    implementation("org.jetbrains.exposed:exposed-spring-boot-starter:0.44.0")
    implementation("io.trbl:blurhash:1.0.0")
    implementation("software.amazon.awssdk:s3:2.20.157")



    implementation("io.ktor:ktor-client-logging-jvm:$ktor_version")

    testImplementation("org.jetbrains.kotlin:kotlin-test-junit:$kotlin_version")
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.6.4")

    implementation("io.ktor:ktor-client-core:$ktor_version")
    implementation("io.ktor:ktor-client-cio:$ktor_version")
    implementation("io.ktor:ktor-client-content-negotiation:$ktor_version")
    testImplementation("io.ktor:ktor-client-mock:$ktor_version")
    implementation("tech.barbero.http-messages-signing:http-messages-signing-core:1.0.0")

    testImplementation("org.junit.jupiter:junit-jupiter:5.8.1")
    testImplementation("org.mockito.kotlin:mockito-kotlin:4.1.0")
    testImplementation("org.mockito:mockito-inline:5.2.0")


    implementation("org.drewcarlson:kjob-core:0.6.0")
    implementation("org.drewcarlson:kjob-mongo:0.6.0")
    testImplementation("org.slf4j:slf4j-simple:2.0.7")

    detektPlugins("io.gitlab.arturbosch.detekt:detekt-formatting:1.22.0")
}

detekt {
    parallel = true
    config = files("detekt.yml")
    buildUponDefaultConfig = true
    basePath = rootDir.absolutePath
    autoCorrect = true
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
