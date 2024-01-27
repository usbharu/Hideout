package dev.usbharu.hideout.mastodon.domain.model

import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document
import java.time.Instant

@Document
data class MastodonNotification(
    @Id
    val id: Long,
    val type: NotificationType,
    val createdAt: Instant,
    val accountId: Long,
    val statusId: Long?,
    val reportId: Long?,
    val relationshipServeranceEvent: Long?
)
