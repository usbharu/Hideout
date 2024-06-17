package dev.usbharu.hideout.core.domain.model.emoji

import org.junit.jupiter.api.Assertions.assertDoesNotThrow
import org.junit.jupiter.api.Test

class EmojiIdTest {
    @Test
    fun emojiIdは0以上である必要がある() {
        org.junit.jupiter.api.assertThrows<IllegalArgumentException> {
            EmojiId(-1)
        }
    }

    @Test
    fun emojiIdは0以上なら設定できる() {
        assertDoesNotThrow {
            EmojiId(1)
        }
    }
}