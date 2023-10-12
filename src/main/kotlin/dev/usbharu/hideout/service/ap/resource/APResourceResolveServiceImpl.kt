package dev.usbharu.hideout.service.ap.resource

import dev.usbharu.hideout.domain.model.ap.Object
import dev.usbharu.hideout.domain.model.hideout.entity.User
import dev.usbharu.hideout.repository.UserRepository
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import kotlinx.coroutines.delay
import java.time.Instant
import java.util.*
import java.util.concurrent.ConcurrentHashMap

class APResourceResolveServiceImpl(private val httpClient: HttpClient, private val userRepository: UserRepository) :
    APResourceResolveService {

    override suspend fun resolve(url: String, singerId: Long?): Object {
        return internalResolve(url, singerId)
    }

    override suspend fun resolve(url: String, singer: User?): Object {
        return internalResolve(url, singer)
    }

    private suspend fun internalResolve(url: String, singerId: Long?): Object {

        val key = genCacheKey(url, singerId)
        val ifAbsent = cacheKey.putIfAbsent(key, Instant.now().toEpochMilli())
        if (ifAbsent == null) {
            val resolve = runResolve(url, singerId?.let { userRepository.findById(it) })
            valueStore.putIfAbsent(key, resolve)
            return resolve
        }
        return wait(key)
    }

    private suspend fun internalResolve(url: String, singer: User?): Object {
        val key = genCacheKey(url, singer?.id)
        val ifAbsent = cacheKey.putIfAbsent(key, Instant.now().toEpochMilli())
        if (ifAbsent == null) {
            val resolve = runResolve(url, singer)
            valueStore.putIfAbsent(key, resolve)
            return resolve
        }
        return wait(key)
    }

    private suspend fun wait(key: String): Object {
        while (valueStore.containsKey(key).not()) {
            delay(1)
        }
        return valueStore.getValue(key) as Object
    }

    private suspend fun runResolve(url: String, singer: User?): Object {
        return httpClient.get(url).body<Object>()
    }

    private fun genCacheKey(url: String, singerId: Long?): String {
        if (singerId != null) {
            return "$url-$singerId"
        }
        return url
    }

    companion object {
        private val cacheKey = ConcurrentHashMap<String, Long>()
        private val valueStore = Collections.synchronizedMap(mutableMapOf<String, Object>())
    }
}
