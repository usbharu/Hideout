package dev.usbharu.hideout.mastodon.service.timeline

import dev.usbharu.hideout.application.external.Transaction
import dev.usbharu.hideout.application.infrastructure.exposed.Page
import dev.usbharu.hideout.application.infrastructure.exposed.PaginationList
import dev.usbharu.hideout.core.service.timeline.GenerateTimelineService
import dev.usbharu.hideout.domain.mastodon.model.generated.Status
import org.springframework.stereotype.Service

@Suppress("LongParameterList")
interface TimelineApiService {

    suspend fun publicTimeline(
        localOnly: Boolean = false,
        remoteOnly: Boolean = false,
        mediaOnly: Boolean = false,
        page: Page
    ): PaginationList<Status, Long>

    suspend fun homeTimeline(
        userId: Long,
        page: Page
    ): PaginationList<Status, Long>
}

@Service
class TimelineApiServiceImpl(
    private val generateTimelineService: GenerateTimelineService,
    private val transaction: Transaction
) : TimelineApiService {

    override suspend fun publicTimeline(
        localOnly: Boolean,
        remoteOnly: Boolean,
        mediaOnly: Boolean,
        page: Page
    ): PaginationList<Status, Long> = transaction.transaction {
        return@transaction generateTimelineService.getTimeline(forUserId = 0, localOnly, mediaOnly, page)
    }

    override suspend fun homeTimeline(userId: Long, page: Page): PaginationList<Status, Long> =
        transaction.transaction {
            return@transaction generateTimelineService.getTimeline(forUserId = userId, page = page)
        }
}
