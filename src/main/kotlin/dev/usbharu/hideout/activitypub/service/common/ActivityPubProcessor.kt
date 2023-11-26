package dev.usbharu.hideout.activitypub.service.common

import dev.usbharu.hideout.activitypub.domain.model.objects.Object

interface ActivityPubProcessor<T : Object> {
    suspend fun process(activity: ActivityPubProcessContext<T>)

    fun isSupported(activityType: ActivityType): Boolean

    fun type(): Class<T>
}
