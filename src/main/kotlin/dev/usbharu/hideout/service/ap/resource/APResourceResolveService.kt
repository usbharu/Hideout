package dev.usbharu.hideout.service.ap.resource

import dev.usbharu.hideout.domain.model.ap.Object
import dev.usbharu.hideout.domain.model.hideout.entity.User

interface APResourceResolveService {
    suspend fun <T : Object> resolve(url: String, clazz: Class<T>, singer: User?): T
    suspend fun <T : Object> resolve(url: String, clazz: Class<T>, singerId: Long?): T
}

suspend inline fun <reified T : Object> APResourceResolveService.resolve(url: String, singer: User?): T {
    return resolve(url, T::class.java, singer)
}

suspend inline fun <reified T : Object> APResourceResolveService.resolve(url: String, singerId: Long?): T {
    return resolve(url, T::class.java, singerId)
}
