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

            val cached = cacheKey.get(key)
            if (cached == null) {
                needRunBlock = true
                cacheKey[key] = Instant.now().toEpochMilli()

                valueStore.remove(key)
            } else {
                needRunBlock = false
            }
        }
        if (needRunBlock) {
            val processed = block()

            if (cacheKey.containsKey(key)) {
                valueStore[key] = processed
            }
        }
    }

    override suspend fun getOrWait(key: String): ResolveResponse {
        while (valueStore.contains(key).not()) {
            if (cacheKey.containsKey(key).not()) {
                throw IllegalStateException("Invalid cache key.")
            }
            delay(1)
        }
        return valueStore.getValue(key)
    }
}
