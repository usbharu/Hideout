package dev.usbharu.hideout.service.ap.resource

import com.fasterxml.jackson.databind.ObjectMapper
import dev.usbharu.hideout.domain.model.ap.Object
import dev.usbharu.hideout.domain.model.hideout.entity.User
import dev.usbharu.hideout.repository.UserRepository
import dev.usbharu.hideout.util.HttpUtil.Activity
import io.ktor.client.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.stereotype.Service

@Service
class APResourceResolveServiceImpl(
    private val httpClient: HttpClient,
    private val userRepository: UserRepository,
    private val cacheManager: CacheManager,
    @Qualifier("activitypub") private val objectMapper: ObjectMapper
) :
    APResourceResolveService {

    override suspend fun <T : Object> resolve(url: String, clazz: Class<T>, singerId: Long?): T {
        return internalResolve(url, singerId, clazz)
    }

    override suspend fun <T : Object> resolve(url: String, clazz: Class<T>, singer: User?): T {
        return internalResolve(url, singer, clazz)
    }

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

    private suspend fun <T : Object> runResolve(url: String, singer: User?, clazz: Class<T>): Object {
        val bodyAsText = httpClient.get(url) {
            header("Accept", ContentType.Application.Activity)
        }.bodyAsText()
        return objectMapper.readValue(bodyAsText, clazz)
    }

    private fun genCacheKey(url: String, singerId: Long?): String {
        if (singerId != null) {
            return "$url-$singerId"
        }
        return url
    }
}
