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

package dev.usbharu.hideout.activitypub.service.common

import dev.usbharu.hideout.activitypub.domain.model.objects.Object
import dev.usbharu.hideout.core.domain.model.actor.Actor
import dev.usbharu.hideout.core.domain.model.actor.ActorRepository
import dev.usbharu.hideout.core.service.resource.CacheManager
import dev.usbharu.hideout.core.service.resource.ResolveResponse
import org.springframework.stereotype.Service
import java.io.InputStream

@Service
class APResourceResolveServiceImpl(
    private val apRequestService: APRequestService,
    private val actorRepository: ActorRepository,
    private val cacheManager: CacheManager
) :
    APResourceResolveService {

    override suspend fun <T : Object> resolve(url: String, clazz: Class<T>, singerId: Long?): T =
        internalResolve(url, singerId, clazz)

    override suspend fun <T : Object> resolve(url: String, clazz: Class<T>, singer: Actor?): T =
        internalResolve(url, singer, clazz)

    private suspend fun <T : Object> internalResolve(url: String, singerId: Long?, clazz: Class<T>): T {
        val key = genCacheKey(url, singerId)

        cacheManager.putCache(key) {
            runResolve(url, singerId?.let { actorRepository.findById(it) }, clazz)
        }
        return (cacheManager.getOrWait(key) as APResolveResponse<T>).objects
    }

    private suspend fun <T : Object> internalResolve(url: String, singer: Actor?, clazz: Class<T>): T {
        val key = genCacheKey(url, singer?.id)
        cacheManager.putCache(key) {
            runResolve(url, singer, clazz)
        }
        return (cacheManager.getOrWait(key) as APResolveResponse<T>).objects
    }

    private suspend fun <T : Object> runResolve(url: String, singer: Actor?, clazz: Class<T>): ResolveResponse =
        APResolveResponse(apRequestService.apGet(url, singer, clazz))

    private fun genCacheKey(url: String, singerId: Long?): String {
        if (singerId != null) {
            return "$url-$singerId"
        }
        return url
    }

    private class APResolveResponse<T : Object>(val objects: T) : ResolveResponse {
        override suspend fun body(): InputStream {
            TODO("Not yet implemented")
        }

        override suspend fun bodyAsText(): String {
            TODO("Not yet implemented")
        }

        override suspend fun bodyAsBytes(): ByteArray {
            TODO("Not yet implemented")
        }

        override suspend fun header(): Map<String, List<String>> {
            TODO("Not yet implemented")
        }

        override suspend fun status(): Int {
            TODO("Not yet implemented")
        }

        override suspend fun statusMessage(): String {
            TODO("Not yet implemented")
        }

        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false

            other as APResolveResponse<*>

            return objects == other.objects
        }

        override fun hashCode(): Int = objects.hashCode()
    }
}
