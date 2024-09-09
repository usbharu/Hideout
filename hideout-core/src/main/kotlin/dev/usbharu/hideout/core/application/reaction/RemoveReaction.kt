package dev.usbharu.hideout.core.application.reaction

data class RemoveReaction(
    val postId: Long,
    val customEmojiId: Long?,
    val unicodeEmoji: String
)
