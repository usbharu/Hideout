package dev.usbharu.hideout.core.service.resource

import io.ktor.client.*
import io.ktor.client.request.*
import org.springframework.stereotype.Service

@Service
open class KtorResourceResolveService(private val httpClient: HttpClient, private val cacheManager: CacheManager) :
    ResourceResolveService {
    override suspend fun resolve(url: String): ResolveResponse {
        cacheManager.putCache(getCacheKey(url)) {
            runResolve(url)
        }
        return cacheManager.getOrWait(getCacheKey(url))
    }

    protected suspend fun runResolve(url: String): ResolveResponse {
        val httpResponse = httpClient.get(url)

        return KtorResolveResponse(httpResponse)
    }

    protected suspend fun getCacheKey(url: String) = url
}
