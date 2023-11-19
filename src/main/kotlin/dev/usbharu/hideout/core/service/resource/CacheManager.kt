package dev.usbharu.hideout.core.service.resource

interface CacheManager {
    suspend fun putCache(key: String, block: suspend () -> ResolveResponse)
    suspend fun getOrWait(key: String): ResolveResponse
}
