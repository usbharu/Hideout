package dev.usbharu.hideout.mastodon.query

import dev.usbharu.hideout.application.infrastructure.exposed.Page
import dev.usbharu.hideout.application.infrastructure.exposed.PaginationList
import dev.usbharu.hideout.domain.mastodon.model.generated.Status
import dev.usbharu.hideout.mastodon.interfaces.api.status.StatusQuery

interface StatusQueryService {
    suspend fun findByPostIds(ids: List<Long>): List<Status>
    suspend fun findByPostIdsWithMediaIds(statusQueries: List<StatusQuery>): List<Status>

    suspend fun accountsStatus(
        accountId: Long,
        onlyMedia: Boolean = false,
        excludeReplies: Boolean = false,
        excludeReblogs: Boolean = false,
        pinned: Boolean = false,
        tagged: String?,
        includeFollowers: Boolean = false,
        page: Page
    ): PaginationList<Status, Long>

    suspend fun findByPostId(id: Long): Status
}
