package dev.usbharu.hideout.core.application.reaction

data class CreateReaction(val postId: Long, val customEmojiId: Long?, val unicodeEmoji: String)
