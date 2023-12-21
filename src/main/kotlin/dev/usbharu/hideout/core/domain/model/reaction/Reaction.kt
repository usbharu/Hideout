package dev.usbharu.hideout.core.domain.model.reaction

import dev.usbharu.hideout.core.domain.model.emoji.Emoji

data class Reaction(val id: Long, val emoji: Emoji, val postId: Long, val actorId: Long)
