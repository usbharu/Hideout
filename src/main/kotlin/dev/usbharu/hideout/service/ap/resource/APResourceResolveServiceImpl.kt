package dev.usbharu.hideout.service.ap.resource

import dev.usbharu.hideout.domain.model.ap.Object
import dev.usbharu.hideout.domain.model.hideout.entity.User
import dev.usbharu.hideout.repository.UserRepository
import dev.usbharu.hideout.service.ap.APRequestService
import org.springframework.stereotype.Service

@Service
class APResourceResolveServiceImpl(
    private val apRequestService: APRequestService,
    private val userRepository: UserRepository,
    private val cacheManager: CacheManager
) :
    APResourceResolveService {

    override suspend fun <T : Object> resolve(url: String, clazz: Class<T>, singerId: Long?): T =
        internalResolve(url, singerId, clazz)

    override suspend fun <T : Object> resolve(url: String, clazz: Class<T>, singer: User?): T =
        internalResolve(url, singer, clazz)

    private suspend fun <T : Object> internalResolve(url: String, singerId: Long?, clazz: Class<T>): T {
        val key = genCacheKey(url, singerId)

        cacheManager.putCache(key) {
            runResolve(url, singerId?.let { userRepository.findById(it) }, clazz)
        }
        return cacheManager.getOrWait(key) as T
    }

    private suspend fun <T : Object> internalResolve(url: String, singer: User?, clazz: Class<T>): T {
        val key = genCacheKey(url, singer?.id)
        cacheManager.putCache(key) {
            runResolve(url, singer, clazz)
        }
        return cacheManager.getOrWait(key) as T
    }

    private suspend fun <T : Object> runResolve(url: String, singer: User?, clazz: Class<T>): Object =
        apRequestService.apGet(url, singer, clazz)

    private fun genCacheKey(url: String, singerId: Long?): String {
        if (singerId != null) {
            return "$url-$singerId"
        }
        return url
    }
}
