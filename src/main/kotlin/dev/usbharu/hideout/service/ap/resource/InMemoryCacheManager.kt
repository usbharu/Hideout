package dev.usbharu.hideout.service.ap.resource

import dev.usbharu.hideout.domain.model.ap.Object
import kotlinx.coroutines.delay
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import org.springframework.stereotype.Service

@Service
class InMemoryCacheManager : CacheManager {
    private val cacheKey = mutableSetOf<String>()
    private val valueStore = mutableMapOf<String, Object>()
    private val keyMutex = Mutex()

    override suspend fun putCache(key: String, block: suspend () -> Object) {
        val hasCache: Boolean
        keyMutex.withLock {
            hasCache = cacheKey.contains(key)
            cacheKey.add(key)
        }
        if (hasCache.not()) {
            val processed = block()

            valueStore[key] = processed

        }
    }

    override suspend fun getOrWait(key: String): Object {

        while (valueStore.contains(key).not()) {
            delay(1)
        }
        return valueStore.getValue(key)

    }
}
