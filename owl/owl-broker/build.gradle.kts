plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.protobuf.plugin)
}


group = "dev.usbharu"

repositories {
    mavenCentral()
}

dependencies {
    implementation(libs.bundles.grpc.kotlin)
    implementation(libs.coroutines.core)
    implementation(project(":owl-common"))
    implementation(libs.log4j2.slf4j)
    implementation(platform(libs.koin.bom))
    implementation(libs.koin.core)
}

tasks.test {
    useJUnitPlatform()
}
kotlin {
    jvmToolchain(21)
}

protobuf {
    protoc {
        artifact = libs.protoc.asProvider().get().toString()
    }
    plugins {
        create("grpc") {
            artifact = libs.protoc.gen.grpc.java.get().toString()
        }
        create("grpckt") {
            artifact = libs.protoc.gen.grpc.kotlin.get().toString() + ":jdk8@jar"
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