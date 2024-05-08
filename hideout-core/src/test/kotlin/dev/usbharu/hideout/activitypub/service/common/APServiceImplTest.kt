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

package dev.usbharu.hideout.activitypub.service.common

import dev.usbharu.hideout.activitypub.domain.exception.JsonParseException
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.kotlin.mock
import utils.JsonObjectMapper.objectMapper
import kotlin.test.assertEquals

class APServiceImplTest {
    @Test
    fun `parseActivity 正常なActivityをパースできる`() {
        val apServiceImpl = APServiceImpl(
            objectMapper = objectMapper, owlProducer = mock()
        )

        //language=JSON
        val activityType = apServiceImpl.parseActivity("""{"type": "Follow"}""")

        assertEquals(ActivityType.Follow, activityType)
    }

    @Test
    fun `parseActivity Typeが配列のActivityをパースできる`() {
        val apServiceImpl = APServiceImpl(
            objectMapper = objectMapper, owlProducer = mock()
        )

        //language=JSON
        val activityType = apServiceImpl.parseActivity("""{"type": ["Follow"]}""")

        assertEquals(ActivityType.Follow, activityType)
    }

    @Test
    fun `parseActivity Typeが配列で関係ない物が入っていてもパースできる`() {
        val apServiceImpl = APServiceImpl(
            objectMapper = objectMapper, owlProducer = mock()
        )

        //language=JSON
        val activityType = apServiceImpl.parseActivity("""{"type": ["Hello","Follow"]}""")

        assertEquals(ActivityType.Follow, activityType)
    }

    @Test
    fun `parseActivity jsonとして解釈できない場合JsonParseExceptionがthrowされる`() {
        val apServiceImpl = APServiceImpl(

            objectMapper = objectMapper, owlProducer = mock()
        )

        //language=JSON
        assertThrows<JsonParseException> {
            apServiceImpl.parseActivity("""hoge""")
        }
    }

    @Test
    fun `parseActivity 空の場合JsonParseExceptionがthrowされる`() {
        val apServiceImpl = APServiceImpl(

            objectMapper = objectMapper, owlProducer = mock()
        )

        //language=JSON
        assertThrows<JsonParseException> {
            apServiceImpl.parseActivity("")
        }
    }

    @Test
    fun `parseActivity jsonにtypeプロパティがない場合JsonParseExceptionがthrowされる`() {
        val apServiceImpl = APServiceImpl(

            objectMapper = objectMapper, owlProducer = mock()
        )

        //language=JSON
        assertThrows<JsonParseException> {
            apServiceImpl.parseActivity("""{"actor": "https://example.com"}""")
        }
    }

    @Test
    fun `parseActivity typeが配列でないときtypeが未定義の場合IllegalArgumentExceptionがthrowされる`() {
        val apServiceImpl = APServiceImpl(

            objectMapper = objectMapper, owlProducer = mock()
        )

        //language=JSON
        assertThrows<IllegalArgumentException> {
            apServiceImpl.parseActivity("""{"type": "Hoge"}""")
        }
    }

    @Test
    fun `parseActivity typeが配列のとき定義済みのtypeを見つけられなかった場合IllegalArgumentExceptionがthrowされる`() {
        val apServiceImpl = APServiceImpl(

            objectMapper = objectMapper, owlProducer = mock()
        )

        //language=JSON
        assertThrows<IllegalArgumentException> {
            apServiceImpl.parseActivity("""{"type": ["Hoge","Fuga"]}""")
        }
    }

    @Test
    fun `parseActivity typeが空の場合IllegalArgumentExceptionがthrowされる`() {
        val apServiceImpl = APServiceImpl(

            objectMapper = objectMapper, owlProducer = mock()
        )

        //language=JSON
        assertThrows<IllegalArgumentException> {
            apServiceImpl.parseActivity("""{"type": ""}""")
        }
    }

    @Test
    fun `parseActivity typeに指定されている文字の判定がcase-insensitiveで行われる`() {
        val apServiceImpl = APServiceImpl(

            objectMapper = objectMapper, owlProducer = mock()
        )

        //language=JSON
        val activityType = apServiceImpl.parseActivity("""{"type": "FoLlOw"}""")

        assertEquals(ActivityType.Follow, activityType)
    }

    @Test
    fun `parseActivity typeが配列のとき指定されている文字の判定がcase-insensitiveで行われる`() {
        val apServiceImpl = APServiceImpl(

            objectMapper = objectMapper, owlProducer = mock()
        )

        //language=JSON
        val activityType = apServiceImpl.parseActivity("""{"type": ["HoGE","fOllOw"]}""")

        assertEquals(ActivityType.Follow, activityType)
    }

    @Test
    fun `parseActivity activityがarrayのときJsonParseExceptionがthrowされる`() {
        val apServiceImpl = APServiceImpl(

            objectMapper = objectMapper, owlProducer = mock()
        )

        //language=JSON
        assertThrows<JsonParseException> {
            apServiceImpl.parseActivity("""[{"type": "Follow"},{"type": "Accept"}]""")
        }
    }

    @Test
    fun `parseActivity activityがvalueのときJsonParseExceptionがthrowされる`() {
        val apServiceImpl = APServiceImpl(

            objectMapper = objectMapper, owlProducer = mock()
        )

        //language=JSON
        assertThrows<IllegalArgumentException> {
            apServiceImpl.parseActivity(""""hoge"""")
        }
    }
}
