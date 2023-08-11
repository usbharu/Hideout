package dev.usbharu.hideout.service.ap

import dev.usbharu.hideout.domain.model.ActivityPubResponse
import dev.usbharu.hideout.domain.model.ap.Like

interface APLikeService {
    suspend fun receiveLike(like: Like): ActivityPubResponse
}
