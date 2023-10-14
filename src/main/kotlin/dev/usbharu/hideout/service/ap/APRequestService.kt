package dev.usbharu.hideout.service.ap

import dev.usbharu.hideout.domain.model.ap.Object
import dev.usbharu.hideout.domain.model.hideout.entity.User

interface APRequestService {
    suspend fun <R : Object> apGet(url: String, signer: User? = null, responseClass: Class<R>): R
    suspend fun <T : Object, R : Object> apPost(
        url: String,
        body: T? = null,
        signer: User? = null,
        responseClass: Class<R>
    ): R

    suspend fun <T : Object> apPost(url: String, body: T? = null, signer: User? = null): String
}

suspend inline fun <reified R : Object> APRequestService.apGet(url: String, signer: User? = null): R =
    apGet(url, signer, R::class.java)

suspend inline fun <T : Object, reified R : Object> APRequestService.apPost(
    url: String,
    body: T? = null,
    signer: User? = null
): R = apPost(url, body, signer, R::class.java)
