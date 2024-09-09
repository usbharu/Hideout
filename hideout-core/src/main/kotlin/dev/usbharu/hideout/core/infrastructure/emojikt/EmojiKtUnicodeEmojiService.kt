package dev.usbharu.hideout.core.infrastructure.emojikt

import Emojis
import dev.usbharu.hideout.core.domain.service.emoji.UnicodeEmojiService
import org.springframework.stereotype.Service

@Service
class EmojiKtUnicodeEmojiService : UnicodeEmojiService {
    override fun isUnicodeEmoji(emoji: String): Boolean = Emojis.allEmojis.singleOrNull { it.char == emoji } != null
}
