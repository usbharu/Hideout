package dev.usbharu.hideout.config

@Deprecated("Config is deprecated")
object Config {
    var configData: ConfigData = ConfigData()
}

@Deprecated("Config is deprecated")
data class ConfigData(
    val url: String = "",
    val domain: String = url.substringAfter("://").substringBeforeLast(":"),
    val characterLimit: CharacterLimit = CharacterLimit()
)
