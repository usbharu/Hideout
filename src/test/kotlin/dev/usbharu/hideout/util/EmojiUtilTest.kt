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

package dev.usbharu.hideout.util

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource

class EmojiUtilTest {

    @Test
    fun 絵文字を判定できる() {
        val emoji = "❤"
        val actual = EmojiUtil.isEmoji(emoji)

        assertThat(actual).isTrue()
    }

    @Test
    fun ただの文字を判定できる() {
        val moji = "blobblinkhyper"
        val actual = EmojiUtil.isEmoji(moji)

        assertThat(actual).isFalse()
    }

    @ParameterizedTest
    @ValueSource(strings = ["❤", "🌄", "🤗", "⛺", "🧑‍🤝‍🧑", "🖐🏿"])
    fun `絵文字判定`(s: String) {
        val actual = EmojiUtil.isEmoji(s)

        assertThat(actual).isTrue()
    }

    @ParameterizedTest
    @ValueSource(strings = ["™", "㍂", "㌠"])
    fun `文字判定`(s: String) {
        val actual = EmojiUtil.isEmoji(s)

        assertThat(actual).isFalse()
    }
}
