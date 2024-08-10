plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "0.8.0"
}
rootProject.name = "hideout-core"

//ローカルで変更した時、リリースまではアンコメント リリース後はコメントアウト
//includeBuild("../owl")

dependencyResolutionManagement {
    repositories {
        mavenCentral()
        maven {
            url = uri("https://git.usbharu.dev/api/packages/usbharu/maven")
        }
    }

    versionCatalogs {
        create("libs") {
            from(files("../libs.versions.toml"))
        }
    }
}