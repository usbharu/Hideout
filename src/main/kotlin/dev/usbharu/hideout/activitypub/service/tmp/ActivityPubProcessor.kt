package dev.usbharu.hideout.activitypub.service.tmp

import dev.usbharu.hideout.activitypub.domain.model.objects.Object
import dev.usbharu.hideout.activitypub.service.common.ActivityType

interface ActivityPubProcessor<T : Object> {
    suspend fun process(activity: T)

    fun isSupported(activityType: ActivityType): Boolean
}
