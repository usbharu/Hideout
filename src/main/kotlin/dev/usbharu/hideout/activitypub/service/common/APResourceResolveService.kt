package dev.usbharu.hideout.activitypub.service.common

import dev.usbharu.hideout.activitypub.domain.model.objects.Object
import dev.usbharu.hideout.core.domain.model.actor.Actor

interface APResourceResolveService {
    suspend fun <T : Object> resolve(url: String, clazz: Class<T>, singer: Actor?): T
    suspend fun <T : Object> resolve(url: String, clazz: Class<T>, singerId: Long?): T
}

suspend inline fun <reified T : Object> APResourceResolveService.resolve(url: String, singer: Actor?): T =
    resolve(url, T::class.java, singer)

suspend inline fun <reified T : Object> APResourceResolveService.resolve(url: String, singerId: Long?): T =
    resolve(url, T::class.java, singerId)
