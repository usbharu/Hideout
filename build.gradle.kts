import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

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
    id("io.gitlab.arturbosch.detekt") version "1.22.0"
    id("com.google.devtools.ksp") version "1.8.21-1.0.11"
//    id("org.jetbrains.kotlin.plugin.serialization") version "1.8.10"
}

group = "dev.usbharu"
version = "0.0.1"
application {
    mainClass.set("io.ktor.server.cio.EngineMain")

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

repositories {
    mavenCentral()
}

kotlin {
    target {
        compilations.all {
            kotlinOptions.jvmTarget = JavaVersion.VERSION_11.toString()
        }
    }
}

sourceSets.main {
    java.srcDirs("build/generated/ksp/main/kotlin")
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
    implementation("ch.qos.logback:logback-classic:$logback_version")

    implementation("io.insert-koin:koin-core:$koin_version")
    implementation("io.insert-koin:koin-ktor:$koin_version")
    implementation("io.insert-koin:koin-logger-slf4j:$koin_version")
    implementation("io.insert-koin:koin-annotations:1.2.0")
    ksp("io.insert-koin:koin-ksp-compiler:1.2.0")


    implementation("io.ktor:ktor-client-logging-jvm:$ktor_version")
    implementation("io.ktor:ktor-server-host-common-jvm:$ktor_version")
    implementation("io.ktor:ktor-server-status-pages-jvm:$ktor_version")

    testImplementation("io.ktor:ktor-server-tests-jvm:$ktor_version")
    testImplementation("org.jetbrains.kotlin:kotlin-test-junit:$kotlin_version")
    testImplementation ("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.6.4")

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
            agent{
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
