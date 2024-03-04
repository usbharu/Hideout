plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "0.5.0"
}
rootProject.name = "owl"
include("common")
include("producer:api")
findProject(":producer:api")?.name = "api"
include("producer:impl")
findProject(":producer:impl")?.name = "impl"
include("broker")
include("broker:broker-mongodb")
findProject(":broker:broker-mongodb")?.name = "broker-mongodb"
