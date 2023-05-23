package dev.usbharu.hideout.service.activitypub

import dev.usbharu.hideout.domain.model.ActivityPubResponse
import dev.usbharu.hideout.domain.model.ap.Create

interface ActivityPubCreateService {
    suspend fun receiveCreate(create: Create): ActivityPubResponse
}
