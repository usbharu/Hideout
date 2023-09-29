package dev.usbharu.hideout.service.post

import dev.usbharu.hideout.domain.mastodon.model.generated.Status
import org.springframework.stereotype.Service

@Service
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
