package dev.usbharu.hideout.domain.model.hideout.dto

data class StatusQuery(
    val postId: Long,
    val replyId: Long?,
    val repostId: Long?,
    val mediaIds: List<Long>
)
