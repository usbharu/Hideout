import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import org.openapitools.generator.gradle.plugin.tasks.GenerateTask

val ktor_version: String by project
val kotlin_version: String by project
val logback_version: String by project
val exposed_version: String by project
val h2_version: String by project
val koin_version: String by project

plugins {
    kotlin("jvm") version "1.8.21"
    id("io.ktor.plugin") version "2.3.0"
    id("org.graalvm.buildtools.native") version "0.9.21"
    id("io.gitlab.arturbosch.detekt") version "1.23.1"
    id("com.google.devtools.ksp") version "1.8.21-1.0.11"
    id("org.springframework.boot") version "3.1.2"
    kotlin("plugin.spring") version "1.8.21"
    id("org.openapi.generator") version "6.6.0"
//    id("org.jetbrains.kotlin.plugin.serialization") version "1.8.10"
}

apply {
    plugin("io.spring.dependency-management")
}

group = "dev.usbharu"
version = "0.0.1"
application {
    mainClass.set("dev.usbharu.hideout.SpringApplicationKt")

    val isDevelopment: Boolean = project.ext.has("development")
    applicationDefaultJvmArgs = listOf("-Dio.ktor.development=$isDevelopment")
}

tasks.withType<Test> {
    useJUnitPlatform()
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompilationTask<*>>().configureEach {
    compilerOptions.languageVersion.set(org.jetbrains.kotlin.gradle.dsl.KotlinVersion.KOTLIN_1_8)
    compilerOptions.apiVersion.set(org.jetbrains.kotlin.gradle.dsl.KotlinVersion.KOTLIN_1_8)
}

tasks.withType<KotlinCompile> {
    kotlinOptions {
        freeCompilerArgs += "-Xjsr305=strict"
    }
    dependsOn("openApiGenerateServer")
    mustRunAfter("openApiGenerateServer")
}

tasks.withType<ShadowJar> {
    manifest {
        attributes(
            "Implementation-Version" to project.version.toString()
        )
    }
}

tasks.clean {
    delete += listOf("$rootDir/src/main/resources/static")
}

tasks.create<GenerateTask>("openApiGenerateServer", GenerateTask::class) {
    generatorName.set("kotlin-spring")
    inputSpec.set("$rootDir/src/main/resources/openapi/api.yaml")
    outputDir.set("$buildDir/generated/sources/openapi")
    apiPackage.set("dev.usbharu.hideout.controller.generated")
    modelPackage.set("dev.usbharu.hideout.domain.model.generated")
    configOptions.put("interfaceOnly", "true")
    configOptions.put("useSpringBoot3", "true")
    additionalProperties.put("useTags", "true")
    schemaMappings.putAll(
        mapOf(
            "ReactionResponse" to "dev.usbharu.hideout.domain.model.hideout.dto.ReactionResponse",
            "Account" to "dev.usbharu.hideout.domain.model.hideout.dto.Account",
            "JwtToken" to "dev.usbharu.hideout.domain.model.hideout.dto.JwtToken",
            "PostRequest" to "dev.usbharu.hideout.domain.model.hideout.form.Post",
            "PostResponse" to "dev.usbharu.hideout.domain.model.hideout.dto.PostResponse",
            "Reaction" to "dev.usbharu.hideout.domain.model.hideout.form.Reaction",
            "RefreshToken" to "dev.usbharu.hideout.domain.model.hideout.form.RefreshToken",
            "UserLogin" to "dev.usbharu.hideout.domain.model.hideout.form.UserLogin",
            "UserResponse" to "dev.usbharu.hideout.domain.model.hideout.dto.UserResponse",
            "UserCreate" to "dev.usbharu.hideout.domain.model.hideout.form.UserCreate",
            "Visibility" to "dev.usbharu.hideout.domain.model.hideout.entity.Visibility",
        )
    )

//    importMappings.putAll(mapOf("ReactionResponse" to "ReactionResponse"))
//    typeMappings.putAll(mapOf("ReactionResponse" to "ReactionResponse"))
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
    kotlin.srcDirs("$buildDir/generated/ksp/main", "$buildDir/generated/sources/openapi/src/main/kotlin")
}

dependencies {
    implementation("io.ktor:ktor-server-core-jvm:$ktor_version")
    implementation("io.ktor:ktor-server-auth:$ktor_version")
    implementation("io.ktor:ktor-server-auth-jwt:$ktor_version")
    implementation("io.ktor:ktor-server-sessions-jvm:$ktor_version")
    implementation("io.ktor:ktor-server-auto-head-response-jvm:$ktor_version")
    implementation("io.ktor:ktor-server-cors-jvm:$ktor_version")
    implementation("io.ktor:ktor-server-default-headers-jvm:$ktor_version")
    implementation("io.ktor:ktor-server-forwarded-header-jvm:$ktor_version")
    implementation("io.ktor:ktor-server-call-logging-jvm:$ktor_version")
    implementation("io.ktor:ktor-server-content-negotiation-jvm:$ktor_version")
    implementation("io.ktor:ktor-serialization-jackson:$ktor_version")
    implementation("org.jetbrains.exposed:exposed-core:$exposed_version")
    implementation("org.jetbrains.exposed:exposed-jdbc:$exposed_version")
    implementation("com.h2database:h2:$h2_version")
    implementation("org.xerial:sqlite-jdbc:3.40.1.0")
    implementation("io.ktor:ktor-server-websockets-jvm:$ktor_version")
    implementation("io.ktor:ktor-server-cio-jvm:$ktor_version")
    implementation("io.ktor:ktor-server-compression:$ktor_version")
    implementation("ch.qos.logback:logback-classic:$logback_version")

    implementation("io.insert-koin:koin-core:$koin_version")
    implementation("io.insert-koin:koin-ktor:$koin_version")
    implementation("io.insert-koin:koin-logger-slf4j:$koin_version")
    implementation("io.insert-koin:koin-annotations:1.2.0")
    implementation("io.ktor:ktor-server-compression-jvm:2.3.0")
    ksp("io.insert-koin:koin-ksp-compiler:1.2.0")

    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-security")
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
    implementation("org.springframework.boot:spring-boot-starter-webflux")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-reactor")
    testImplementation("org.springframework.boot:spring-boot-test-autoconfigure")
    testImplementation("org.springframework.boot:spring-boot-starter-test")

    implementation("io.ktor:ktor-client-logging-jvm:$ktor_version")
    implementation("io.ktor:ktor-server-host-common-jvm:$ktor_version")
    implementation("io.ktor:ktor-server-status-pages-jvm:$ktor_version")

    testImplementation("io.ktor:ktor-server-tests-jvm:$ktor_version")
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
    testImplementation("io.ktor:ktor-server-test-host-jvm:$ktor_version")

    testImplementation("org.slf4j:slf4j-simple:2.0.7")

    detektPlugins("io.gitlab.arturbosch.detekt:detekt-formatting:1.22.0")
}

jib {
    if (System.getProperty("os.name").toLowerCase().contains("windows")) {
        dockerClient.environment = mapOf(
            "DOCKER_HOST" to "localhost:2375"
        )
    }
}

ktor {
    docker {
        localImageName.set("hideout")
    }
}

graalvmNative {
    binaries {
        named("main") {
            fallback.set(false)
            verbose.set(true)
            agent {
                enabled.set(false)
            }

            buildArgs.add("--initialize-at-build-time=io.ktor,kotlin,kotlinx")
//            buildArgs.add("--trace-class-initialization=ch.qos.logback.classic.Logger")
//            buildArgs.add("--trace-object-instantiation=ch.qos.logback.core.AsyncAppenderBase"+"$"+"Worker")
//            buildArgs.add("--trace-object-instantiation=ch.qos.logback.classic.Logger")
            buildArgs.add("--initialize-at-build-time=org.slf4j.LoggerFactory,ch.qos.logback")
//            buildArgs.add("--trace-object-instantiation=kotlinx.coroutines.channels.ArrayChannel")
            buildArgs.add("--initialize-at-build-time=kotlinx.coroutines.channels.ArrayChannel")
            buildArgs.add("-H:+InstallExitHandlers")
            buildArgs.add("-H:+ReportUnsupportedElementsAtRuntime")
            buildArgs.add("-H:+ReportExceptionStackTraces")

            runtimeArgs.add("-config=$buildDir/resources/main/application-native.conf")
            imageName.set("graal-server")
        }
    }
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
