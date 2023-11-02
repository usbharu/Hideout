package dev.usbharu.hideout.core.service.timeline

import dev.usbharu.hideout.domain.mastodon.model.generated.Status
import org.springframework.stereotype.Service

@Service
@Suppress("LongParameterList")
interface GenerateTimelineService {
    suspend fun getTimeline(
        forUserId: Long? = null,
        localOnly: Boolean = false,
        mediaOnly: Boolean = false,
        maxId: Long? = null,
        minId: Long? = null,
        sinceId: Long? = null,
        limit: Int = 20
    ): List<Status>
}