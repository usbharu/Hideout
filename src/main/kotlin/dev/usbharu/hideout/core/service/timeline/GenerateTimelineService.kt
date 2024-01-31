package dev.usbharu.hideout.core.service.timeline

import dev.usbharu.hideout.application.infrastructure.exposed.Page
import dev.usbharu.hideout.application.infrastructure.exposed.PaginationList
import dev.usbharu.hideout.domain.mastodon.model.generated.Status
import org.springframework.stereotype.Service

@Service
@Suppress("LongParameterList")
interface GenerateTimelineService {

    suspend fun getTimeline(
        forUserId: Long? = null,
        localOnly: Boolean = false,
        mediaOnly: Boolean = false,
        page: Page
    ): PaginationList<Status, Long>
}
