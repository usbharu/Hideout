package dev.usbharu.hideout.ap

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import dev.usbharu.hideout.domain.model.ap.Accept
import dev.usbharu.hideout.domain.model.ap.Follow
import org.junit.jupiter.api.Test

class ContextSerializerTest {

    @Test
    fun serialize() {
        val accept = Accept(
            name = "aaa",
            actor = "bbb",
            `object` = Follow(
                name = "ccc",
                `object` = "ddd",
                actor = "aaa"
            )
        )
        val writeValueAsString = jacksonObjectMapper().writeValueAsString(accept)
        println(writeValueAsString)
    }
}
