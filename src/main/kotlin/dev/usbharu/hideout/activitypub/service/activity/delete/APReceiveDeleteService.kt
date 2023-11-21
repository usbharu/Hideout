package dev.usbharu.hideout.activitypub.service.activity.delete

import dev.usbharu.hideout.activitypub.domain.model.Delete

interface APReceiveDeleteService {
    suspend fun receiveDelete(delete: Delete)
}
