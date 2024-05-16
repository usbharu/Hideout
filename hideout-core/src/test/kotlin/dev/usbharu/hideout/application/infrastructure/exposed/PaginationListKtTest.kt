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

package dev.usbharu.hideout.application.infrastructure.exposed

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class PaginationListKtTest {
    @Test
    fun `toHttpHeader nextとprevがnullでない場合両方作成される`() {
        val paginationList = PaginationList<String, Long>(emptyList(), 1, 2)

        val httpHeader =
            paginationList.toHttpHeader({ "https://example.com?max_id=$it" }, { "https://example.com?min_id=$it" })

        assertThat(httpHeader).isEqualTo("<https://example.com?max_id=1>; rel=\"next\", <https://example.com?min_id=2>; rel=\"prev\"")
    }

    @Test
    fun `toHttpHeader nextがnullなら片方だけ作成される`() {
        val paginationList = PaginationList<String, Long>(emptyList(), 1,null)

        val httpHeader =
            paginationList.toHttpHeader({ "https://example.com?max_id=$it" }, { "https://example.com?min_id=$it" })

        assertThat(httpHeader).isEqualTo("<https://example.com?max_id=1>; rel=\"next\"")
    }

    @Test
    fun `toHttpHeader prevがnullなら片方だけ作成される`() {
        val paginationList = PaginationList<String, Long>(emptyList(), null,2)

        val httpHeader =
            paginationList.toHttpHeader({ "https://example.com?max_id=$it" }, { "https://example.com?min_id=$it" })

        assertThat(httpHeader).isEqualTo("<https://example.com?min_id=2>; rel=\"prev\"")
    }

    @Test
    fun `toHttpHeader 両方nullならnullが返ってくる`() {
        val paginationList = PaginationList<String, Long>(emptyList(), null, null)


        val httpHeader =
            paginationList.toHttpHeader({ "https://example.com?max_id=$it" }, { "https://example.com?min_id=$it" })

        assertThat(httpHeader).isNull()
    }
}