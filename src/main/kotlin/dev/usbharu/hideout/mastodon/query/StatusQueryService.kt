package dev.usbharu.hideout.mastodon.query

import dev.usbharu.hideout.domain.mastodon.model.generated.Status
import dev.usbharu.hideout.mastodon.interfaces.api.status.StatusQuery

interface StatusQueryService {
    suspend fun findByPostIds(ids: List<Long>): List<Status>
    suspend fun findByPostIdsWithMediaIds(statusQueries: List<StatusQuery>): List<Status>
}
