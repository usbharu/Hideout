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

import dev.usbharu.hideout.util.LruCache
import kotlinx.coroutines.delay
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import org.springframework.stereotype.Service
import java.time.Instant

@Service
class InMemoryCacheManager : CacheManager {
    private val cacheKey = LruCache<String, Long>(15)
    private val valueStore = mutableMapOf<String, ResolveResponse>()
    private val keyMutex = Mutex()

    override suspend fun putCache(key: String, block: suspend () -> ResolveResponse) {
        val needRunBlock: Boolean
        keyMutex.withLock {
            cacheKey.filter { Instant.ofEpochMilli(it.value).plusSeconds(300) <= Instant.now() }

            val cached = cacheKey[key]
            if (cached == null) {
                needRunBlock = true
                cacheKey[key] = Instant.now().toEpochMilli()

                valueStore.remove(key)
            } else {
                needRunBlock = false
            }
        }
        if (needRunBlock) {
            @Suppress("TooGenericExceptionCaught")
            val processed = try {
                block()
            } catch (e: Exception) {
                cacheKey.remove(key)
                throw e
            }

            if (cacheKey.containsKey(key)) {
                valueStore[key] = processed
            }
        }
    }

    override suspend fun getOrWait(key: String): ResolveResponse {
        while (valueStore.contains(key).not()) {
            if (cacheKey.containsKey(key).not()) {
                throw IllegalStateException("Invalid cache key. $key")
            }
            delay(1)
        }
        return valueStore.getValue(key)
    }
}
