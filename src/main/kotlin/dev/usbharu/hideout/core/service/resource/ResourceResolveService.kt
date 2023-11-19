package dev.usbharu.hideout.core.service.resource

interface ResourceResolveService {
    suspend fun resolve(url: String): ResolveResponse
}
