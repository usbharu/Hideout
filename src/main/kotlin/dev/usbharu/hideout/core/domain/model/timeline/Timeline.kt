package dev.usbharu.hideout.core.domain.model.timeline

import dev.usbharu.hideout.core.domain.model.post.Visibility
import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document

@Document
data class Timeline(
    @Id
    val id: Long,
    val userId: Long,
    val timelineId: Long,
    val postId: Long,
    val postUserId: Long,
    val createdAt: Long,
    val replyId: Long?,
    val repostId: Long?,
    val visibility: Visibility,
    val sensitive: Boolean,
    val isLocal: Boolean,
    val isPureRepost: Boolean = false,
    val mediaIds: List<Long> = emptyList()
)