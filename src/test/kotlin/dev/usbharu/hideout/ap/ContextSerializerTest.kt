package dev.usbharu.hideout.ap

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import dev.usbharu.hideout.activitypub.domain.model.Accept
import dev.usbharu.hideout.activitypub.domain.model.Follow
import org.junit.jupiter.api.Test

class ContextSerializerTest {

    @Test
    fun serialize() {
        val accept = Accept(
            actor = "bbb",
            apObject = Follow(
                apObject = "ddd",
                actor = "aaa"
            )
        )
        val writeValueAsString = jacksonObjectMapper().writeValueAsString(accept)
        println(writeValueAsString)
    }
}
