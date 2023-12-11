package dev.usbharu.hideout.activitypub.service.common

import dev.usbharu.hideout.activitypub.domain.model.objects.Object
import dev.usbharu.hideout.core.domain.model.actor.Actor

interface APRequestService {
    suspend fun <R : Object> apGet(url: String, signer: Actor? = null, responseClass: Class<R>): R
    suspend fun <T : Object, R : Object> apPost(
        url: String,
        body: T? = null,
        signer: Actor? = null,
        responseClass: Class<R>
    ): R

    suspend fun <T : Object> apPost(url: String, body: T? = null, signer: Actor? = null): String
}

suspend inline fun <reified R : Object> APRequestService.apGet(url: String, signer: Actor? = null): R =
    apGet(url, signer, R::class.java)

suspend inline fun <T : Object, reified R : Object> APRequestService.apPost(
    url: String,
    body: T? = null,
    signer: Actor? = null
): R = apPost(url, body, signer, R::class.java)
