package dev.usbharu.hideout.service.ap

import dev.usbharu.hideout.domain.model.ActivityPubResponse
import dev.usbharu.hideout.domain.model.ap.Accept

interface APAcceptService {
    suspend fun receiveAccept(accept: Accept): ActivityPubResponse
}
