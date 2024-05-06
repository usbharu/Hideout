plugins {
    kotlin("jvm")
    id("com.google.protobuf") version "0.9.4"
    id("com.google.devtools.ksp") version "1.9.23-1.0.20"
}

apply {
    plugin("com.google.devtools.ksp")
}


group = "dev.usbharu"
version = "0.0.1"

repositories {
    mavenCentral()
}

dependencies {
    implementation("io.grpc:grpc-kotlin-stub:1.4.1")
    implementation("io.grpc:grpc-protobuf:1.61.1")
    implementation("com.google.protobuf:protobuf-kotlin:3.25.3")
    implementation("io.grpc:grpc-netty:1.61.1")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.8.0")
    implementation(project(":common"))
    implementation("org.apache.logging.log4j:log4j-slf4j2-impl:2.23.0")
    implementation(platform("io.insert-koin:koin-bom:3.5.6"))
    implementation(platform("io.insert-koin:koin-annotations-bom:1.3.1"))
    implementation("io.insert-koin:koin-core")
    compileOnly("io.insert-koin:koin-annotations")
    ksp("io.insert-koin:koin-ksp-compiler:1.3.1")
}

tasks.test {
    useJUnitPlatform()
}
kotlin {
    jvmToolchain(17)
}

protobuf {
    protoc {
        artifact = "com.google.protobuf:protoc:3.25.3"
    }
    plugins {
        create("grpc") {
            artifact = "io.grpc:protoc-gen-grpc-java:1.61.1"
        }
        create("grpckt") {
            artifact = "io.grpc:protoc-gen-grpc-kotlin:1.4.1:jdk8@jar"
        }
    }
    generateProtoTasks {
        all().forEach {
            it.plugins {
                create("grpc")
                create("grpckt")
            }
            it.builtins {
                create("kotlin")
            }
        }
    }
}