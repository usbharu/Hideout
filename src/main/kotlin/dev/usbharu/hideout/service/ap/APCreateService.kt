package dev.usbharu.hideout.service.ap

import dev.usbharu.hideout.domain.model.ActivityPubResponse
import dev.usbharu.hideout.domain.model.ap.Create

interface APCreateService {
    suspend fun receiveCreate(create: Create): ActivityPubResponse
}
