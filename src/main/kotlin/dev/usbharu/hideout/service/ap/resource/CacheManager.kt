package dev.usbharu.hideout.service.ap.resource

import dev.usbharu.hideout.domain.model.ap.Object

interface CacheManager {

    suspend fun putCache(key: String, block: suspend () -> Object)
    suspend fun getOrWait(key: String): Object
}
