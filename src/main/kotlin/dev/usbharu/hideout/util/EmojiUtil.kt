package dev.usbharu.hideout.util

import Emojis

object EmojiUtil {

    val emojiMap by lazy {
        Emojis.allEmojis
            .associate { it.code.replace(" ", "-") to it.char }
            .filterValues { it != "™" }
    }

    fun isEmoji(string: String): Boolean = emojiMap.any { it.value == string }
}
