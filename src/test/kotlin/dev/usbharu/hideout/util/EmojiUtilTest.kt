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
