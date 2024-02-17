/*
 * Copyright (C) 2024 usbharu
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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
