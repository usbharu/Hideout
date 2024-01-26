package dev.usbharu.hideout.mastodon.interfaces.api.timeline

import dev.usbharu.hideout.controller.mastodon.generated.TimelineApi
import dev.usbharu.hideout.core.infrastructure.springframework.security.LoginUserContextHolder
import dev.usbharu.hideout.domain.mastodon.model.generated.Status
import dev.usbharu.hideout.mastodon.service.timeline.TimelineApiService
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.runBlocking
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Controller

@Controller
class MastodonTimelineApiController(
    private val timelineApiService: TimelineApiService,
    private val loginUserContextHolder: LoginUserContextHolder
) : TimelineApi {
    override fun apiV1TimelinesHomeGet(
        maxId: String?,
        sinceId: String?,
        minId: String?,
        limit: Int?
    ): ResponseEntity<Flow<Status>> = runBlocking {
        val homeTimeline = timelineApiService.homeTimeline(
            userId = loginUserContextHolder.getLoginUserId(),
            maxId = maxId?.toLongOrNull(),
            minId = minId?.toLongOrNull(),
            sinceId = sinceId?.toLongOrNull(),
            limit = limit ?: 20
        )
        ResponseEntity(homeTimeline.asFlow(), HttpStatus.OK)
    }

    override fun apiV1TimelinesPublicGet(
        local: Boolean?,
        remote: Boolean?,
        onlyMedia: Boolean?,
        maxId: String?,
        sinceId: String?,
        minId: String?,
        limit: Int?
    ): ResponseEntity<Flow<Status>> = runBlocking {
        val publicTimeline = timelineApiService.publicTimeline(
            localOnly = local ?: false,
            remoteOnly = remote ?: false,
            mediaOnly = onlyMedia ?: false,
            maxId = maxId?.toLongOrNull(),
            minId = minId?.toLongOrNull(),
            sinceId = sinceId?.toLongOrNull(),
            limit = limit ?: 20
        )
        ResponseEntity(publicTimeline.asFlow(), HttpStatus.OK)
    }
}
