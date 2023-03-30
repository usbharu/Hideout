package dev.usbharu.hideout.config

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper

object Config {
    var configData: ConfigData = ConfigData()
}

data class ConfigData(
    val url: String = "",
    val domain: String = url.substringAfter("://").substringBeforeLast(":"),
    val objectMapper: ObjectMapper = jacksonObjectMapper()
)
