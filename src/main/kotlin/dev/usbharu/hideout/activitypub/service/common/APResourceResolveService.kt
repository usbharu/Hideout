package dev.usbharu.hideout.activitypub.service.common

import dev.usbharu.hideout.activitypub.domain.model.`object`.Object
import dev.usbharu.hideout.core.domain.model.user.User

interface APResourceResolveService {
    suspend fun <T : Object> resolve(url: String, clazz: Class<T>, singer: User?): T
    suspend fun <T : Object> resolve(url: String, clazz: Class<T>, singerId: Long?): T
}

suspend inline fun <reified T : Object> APResourceResolveService.resolve(url: String, singer: User?): T =
    resolve(url, T::class.java, singer)

suspend inline fun <reified T : Object> APResourceResolveService.resolve(url: String, singerId: Long?): T =
    resolve(url, T::class.java, singerId)
