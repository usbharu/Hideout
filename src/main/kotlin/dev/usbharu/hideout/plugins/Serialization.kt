package dev.usbharu.hideout.plugins

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonSetter
import com.fasterxml.jackson.annotation.Nulls
import com.fasterxml.jackson.databind.DeserializationFeature
import io.ktor.serialization.jackson.*
import io.ktor.server.application.*
import io.ktor.server.plugins.contentnegotiation.*

fun Application.configureSerialization() {
    install(ContentNegotiation) {
        jackson {
            enable(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY)
            setSerializationInclusion(JsonInclude.Include.NON_EMPTY)
            configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
            configOverride(List::class.java).setSetterInfo(JsonSetter.Value.forContentNulls(Nulls.AS_EMPTY))
        }
    }
}
