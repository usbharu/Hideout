package dev.usbharu.hideout.util

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue

object JsonUtil {
    val objectMapper = jacksonObjectMapper()

    fun mapToJson(map: Map<*, *>, objectMapper: ObjectMapper = this.objectMapper): String =
        objectMapper.writeValueAsString(map)

    fun <K, V> jsonToMap(json: String, objectMapper: ObjectMapper = this.objectMapper): Map<K, V> =
        objectMapper.readValue(json)
}
