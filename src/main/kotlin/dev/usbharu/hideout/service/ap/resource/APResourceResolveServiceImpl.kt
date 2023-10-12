package dev.usbharu.hideout.service.ap.resource

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import dev.usbharu.hideout.domain.model.ap.Object
import dev.usbharu.hideout.domain.model.hideout.entity.User
import dev.usbharu.hideout.repository.UserRepository
import io.ktor.client.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
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

    override suspend fun resolve(url: String, singerId: Long?): Object {
        return internalResolve(url, singerId)
    }

    override suspend fun resolve(url: String, singer: User?): Object {
        return internalResolve(url, singer)
    }

    private suspend fun internalResolve(url: String, singerId: Long?): Object {

        val key = genCacheKey(url, singerId)

        cacheManager.putCache(key) {
            runResolve(url, singerId?.let { userRepository.findById(it) })
        }
        return cacheManager.getOrWait(key)
    }

    private suspend fun internalResolve(url: String, singer: User?): Object {
        val key = genCacheKey(url, singer?.id)
        cacheManager.putCache(key) {
            runResolve(url, singer)
        }
        return cacheManager.getOrWait(key)
    }

    private suspend fun runResolve(url: String, singer: User?): Object {
        val bodyAsText = httpClient.get(url).bodyAsText()
        return objectMapper.readValue<Object>(bodyAsText)
    }

    private fun genCacheKey(url: String, singerId: Long?): String {
        if (singerId != null) {
            return "$url-$singerId"
        }
        return url
    }
}
