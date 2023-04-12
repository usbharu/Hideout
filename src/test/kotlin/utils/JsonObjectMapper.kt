package utils

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonSetter
import com.fasterxml.jackson.annotation.Nulls
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper

object JsonObjectMapper {
    val objectMapper: com.fasterxml.jackson.databind.ObjectMapper =
        jacksonObjectMapper().enable(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY)
            .setSerializationInclusion(JsonInclude.Include.NON_EMPTY)
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)

    init {
        objectMapper.configOverride(List::class.java).setSetterInfo(
            JsonSetter.Value.forValueNulls(
                Nulls.AS_EMPTY
            )
        )
    }
}
