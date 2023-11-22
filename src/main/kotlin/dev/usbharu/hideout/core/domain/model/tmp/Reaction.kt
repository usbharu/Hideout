package dev.usbharu.hideout.core.domain.model.tmp

data class Reaction(
    val id: Long,
    val emojiId: Long,
    val postId: PostId,
    val userId: UserId
)
