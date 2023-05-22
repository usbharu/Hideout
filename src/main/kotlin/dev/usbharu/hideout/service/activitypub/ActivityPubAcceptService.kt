package dev.usbharu.hideout.service.activitypub

import dev.usbharu.hideout.domain.model.ActivityPubResponse
import dev.usbharu.hideout.domain.model.ap.Accept

interface ActivityPubAcceptService {
    suspend fun receiveAccept(accept: Accept): ActivityPubResponse
}
