plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "0.8.0"
}
rootProject.name = "owl"
include("common")
include("producer:api")
findProject(":producer:api")?.name = "api"
include("broker")
include("broker:broker-mongodb")
findProject(":broker:broker-mongodb")?.name = "broker-mongodb"
include("producer:default")
findProject(":producer:default")?.name = "default"
include("consumer")
