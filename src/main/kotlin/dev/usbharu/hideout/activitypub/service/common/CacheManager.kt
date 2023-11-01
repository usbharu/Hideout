package dev.usbharu.hideout.activitypub.service.common

import dev.usbharu.hideout.activitypub.domain.model.`object`.Object

interface CacheManager {

    suspend fun putCache(key: String, block: suspend () -> Object)
    suspend fun getOrWait(key: String): Object
}
