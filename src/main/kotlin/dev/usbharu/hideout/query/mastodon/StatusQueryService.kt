package dev.usbharu.hideout.query.mastodon

import dev.usbharu.hideout.domain.mastodon.model.generated.Status
import dev.usbharu.hideout.domain.model.hideout.dto.StatusQuery

interface StatusQueryService {
    suspend fun findByPostIds(ids: List<Long>): List<Status>
    suspend fun findByPostIdsWithMediaIds(statusQueries: List<StatusQuery>): List<Status>
}
