plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "0.5.0"
}
rootProject.name = "hideout"
include("application")
include("core")
include("federation:activitypub")
findProject(":federation:activitypub")?.name = "activitypub"
include("mastodon")
include("util")
