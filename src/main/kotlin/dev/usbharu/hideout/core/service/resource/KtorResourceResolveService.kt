package dev.usbharu.hideout.core.service.resource

import dev.usbharu.hideout.application.config.MediaConfig
import dev.usbharu.hideout.core.domain.exception.media.RemoteMediaFileSizeException
import io.ktor.client.*
import io.ktor.client.request.*
import io.ktor.http.*
import org.springframework.stereotype.Service

@Service
open class KtorResourceResolveService(
    private val httpClient: HttpClient,
    private val cacheManager: CacheManager,
    private val mediaConfig: MediaConfig
) :
    ResourceResolveService {

    var sizeLimit = mediaConfig.remoteMediaFileSizeLimit

    override suspend fun resolve(url: String): ResolveResponse {
        cacheManager.putCache(getCacheKey(url)) {
            runResolve(url)
        }
        return cacheManager.getOrWait(getCacheKey(url))
    }

    protected suspend fun runResolve(url: String): ResolveResponse {
        val httpResponse = httpClient.get(url)
        val contentLength = httpResponse.contentLength()
        if ((contentLength ?: sizeLimit) >= sizeLimit) {
            throw RemoteMediaFileSizeException("File size is too large. $contentLength >= $sizeLimit")
        }
        return KtorResolveResponse(httpResponse)
    }

    protected suspend fun getCacheKey(url: String) = url
}
