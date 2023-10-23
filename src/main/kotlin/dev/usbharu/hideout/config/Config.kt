package dev.usbharu.hideout.config

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper

@Deprecated("Config is deprecated")
object Config {
    var configData: ConfigData = ConfigData()
}

@Deprecated("Config is deprecated")
data class ConfigData(
    val url: String = "",
    val domain: String = url.substringAfter("://").substringBeforeLast(":"),
    val objectMapper: ObjectMapper = jacksonObjectMapper(),
    val characterLimit: CharacterLimit = CharacterLimit()
)
