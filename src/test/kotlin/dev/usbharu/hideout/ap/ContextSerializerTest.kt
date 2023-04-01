package dev.usbharu.hideout.ap

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class ContextSerializerTest{

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
