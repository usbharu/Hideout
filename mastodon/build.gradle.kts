import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import org.openapitools.generator.gradle.plugin.tasks.GenerateTask

plugins {
    id("org.openapi.generator") version "7.2.0"
}


repositories {
    mavenCentral()
}

sourceSets.main {
    kotlin.srcDirs(
        "$buildDir/generated/sources/mastodon/src/main/kotlin"
    )
}

dependencies {
    implementation("com.fasterxml.jackson.datatype:jackson-datatype-jsr310")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
    implementation(project(":application"))
    implementation(project(":core"))
    implementation("org.springframework.boot:spring-boot-starter-web")
}

tasks.withType<KotlinCompile> {
    kotlinOptions {
        freeCompilerArgs += "-Xjsr305=strict"
    }
    dependsOn("openApiGenerateMastodonCompatibleApi")
    mustRunAfter("openApiGenerateMastodonCompatibleApi")
}

tasks.create<GenerateTask>("openApiGenerateMastodonCompatibleApi", GenerateTask::class) {
    generatorName.set("kotlin-spring")
    inputSpec.set("$projectDir/src/main/resources/openapi/mastodon.yaml")
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