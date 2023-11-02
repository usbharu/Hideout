package dev.usbharu.hideout.activitypub.service.activity.delete

import dev.usbharu.hideout.activitypub.domain.model.Delete
import dev.usbharu.hideout.activitypub.interfaces.api.common.ActivityPubResponse

interface APReceiveDeleteService {
    suspend fun receiveDelete(delete: Delete): ActivityPubResponse
}
