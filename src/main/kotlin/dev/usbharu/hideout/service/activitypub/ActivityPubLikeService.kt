package dev.usbharu.hideout.service.activitypub

import dev.usbharu.hideout.domain.model.ActivityPubResponse
import dev.usbharu.hideout.domain.model.ap.Like

interface ActivityPubLikeService {
    suspend fun receiveLike(like: Like): ActivityPubResponse
}
