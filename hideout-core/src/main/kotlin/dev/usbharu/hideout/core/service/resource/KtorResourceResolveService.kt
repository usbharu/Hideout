/*
 * Copyright (C) 2024 usbharu
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package dev.usbharu.hideout.core.service.resource

import dev.usbharu.hideout.application.config.MediaConfig
import dev.usbharu.hideout.core.domain.exception.media.RemoteMediaFileSizeException
import io.ktor.client.*
import io.ktor.client.request.*
import io.ktor.http.*
import org.springframework.stereotype.Service

@Service
class KtorResourceResolveService(
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
        if ((contentLength ?: 0) >= sizeLimit) {
            throw RemoteMediaFileSizeException("File size is too large. $contentLength >= $sizeLimit")
        }
        return KtorResolveResponse(httpResponse)
    }

    protected suspend fun getCacheKey(url: String) = url
}
