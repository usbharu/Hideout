package dev.usbharu.hideout.service.activitypub

import dev.usbharu.hideout.ap.Follow
import dev.usbharu.hideout.domain.model.ActivityPubResponse

interface ActivityPubFollowService {
    suspend fun receiveFollow(follow:Follow):ActivityPubResponse
}
