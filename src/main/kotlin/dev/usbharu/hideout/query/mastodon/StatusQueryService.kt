package dev.usbharu.hideout.query.mastodon

import dev.usbharu.hideout.domain.mastodon.model.generated.Status

interface StatusQueryService {
    suspend fun findByPostIds(ids: List<Long>): List<Status>
}
