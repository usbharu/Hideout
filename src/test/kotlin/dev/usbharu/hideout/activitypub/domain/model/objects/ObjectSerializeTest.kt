package dev.usbharu.hideout.activitypub.domain.model.objects

import com.fasterxml.jackson.module.kotlin.readValue
import dev.usbharu.hideout.application.config.ActivityPubConfig
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class ObjectSerializeTest {
    @Test
    fun typeが文字列のときデシリアライズできる() {
        //language=JSON
        val json = """{"type": "Object"}"""

        val objectMapper = ActivityPubConfig().objectMapper()

        val readValue = objectMapper.readValue<Object>(json)

        val expected = Object(
            listOf("Object")
        )
        assertEquals(expected, readValue)
    }

    @Test
    fun typeが文字列の配列のときデシリアライズできる() {
        //language=JSON
        val json = """{"type": ["Hoge","Object"]}"""

        val objectMapper = ActivityPubConfig().objectMapper()

        val readValue = objectMapper.readValue<Object>(json)

        val expected = Object(
            listOf("Hoge", "Object")
        )

        assertEquals(expected, readValue)
    }

    @Test
    fun typeが空のとき無視してデシリアライズする() {
        //language=JSON
        val json = """{"type": ""}"""

        val objectMapper = ActivityPubConfig().objectMapper()

        val readValue = objectMapper.readValue<Object>(json)

        val expected = Object(
            emptyList()
        )

        assertEquals(expected, readValue)
    }

}
