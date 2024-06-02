import org.openapitools.generator.gradle.plugin.tasks.GenerateTask

plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.openapi.generator)
    alias(libs.plugins.spring.boot)
    alias(libs.plugins.kotlin.spring)
}


apply {
    plugin("io.spring.dependency-management")
}

group = "dev.usbharu"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-security")
    implementation("com.fasterxml.jackson.datatype:jackson-datatype-jsr310")

    implementation("dev.usbharu:hideout-core:0.0.1")

    implementation(libs.jackson.databind)
    implementation(libs.jackson.module.kotlin)
    implementation(libs.jakarta.annotation)
    implementation(libs.jakarta.validation)

    implementation(libs.bundles.openapi)
    implementation(libs.bundles.coroutines)
}

tasks.test {
    useJUnitPlatform()
}
kotlin {
    jvmToolchain(21)
}

tasks.create<GenerateTask>("openApiGenerateMastodonCompatibleApi", GenerateTask::class) {
    generatorName.set("kotlin-spring")
    inputSpec.set("$rootDir/src/main/resources/openapi/mastodon.yaml")
    outputDir.set("$buildDir/generated/sources/mastodon")
    apiPackage.set("dev.usbharu.hideout.mastodon.interfaces.api.generated")
    modelPackage.set("dev.usbharu.hideout.mastodon.interfaces.api.generated.model")
    configOptions.put("interfaceOnly", "true")
    configOptions.put("useSpringBoot3", "true")
    configOptions.put("reactive", "true")
    configOptions.put("gradleBuildFile", "false")
    configOptions.put("useSwaggerUI", "false")
    configOptions.put("enumPropertyNaming", "UPPERCASE")
    additionalProperties.put("useTags", "true")

    importMappings.put("org.springframework.core.io.Resource", "org.springframework.web.multipart.MultipartFile")
    typeMappings.put("org.springframework.core.io.Resource", "org.springframework.web.multipart.MultipartFile")
    templateDir.set("$rootDir/templates")
}

sourceSets.main {
    kotlin.srcDirs(
        "$buildDir/generated/sources/mastodon/src/main/kotlin"
    )
}
