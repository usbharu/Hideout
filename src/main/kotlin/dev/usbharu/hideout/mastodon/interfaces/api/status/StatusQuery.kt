package dev.usbharu.hideout.mastodon.interfaces.api.status

data class StatusQuery(
    val postId: Long,
    val replyId: Long?,
    val repostId: Long?,
    val mediaIds: List<Long>,
    val emojiIds: List<Long>
)
