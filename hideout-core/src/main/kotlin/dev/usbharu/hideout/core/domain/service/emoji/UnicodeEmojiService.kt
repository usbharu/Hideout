package dev.usbharu.hideout.core.domain.service.emoji

interface UnicodeEmojiService {
    fun isUnicodeEmoji(emoji: String): Boolean
}