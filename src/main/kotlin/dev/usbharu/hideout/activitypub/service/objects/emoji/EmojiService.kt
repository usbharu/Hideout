package dev.usbharu.hideout.activitypub.service.objects.emoji

import dev.usbharu.hideout.activitypub.domain.model.Emoji
import dev.usbharu.hideout.core.domain.model.emoji.CustomEmoji

interface EmojiService {
    suspend fun fetchEmoji(url: String): Pair<Emoji, CustomEmoji>
    suspend fun fetchEmoji(emoji: Emoji): Pair<Emoji, CustomEmoji>
    suspend fun findByEmojiName(emojiName: String): CustomEmoji?
}
