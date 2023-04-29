package dev.usbharu.hideout.service.signature

import io.ktor.http.*

interface HttpSignatureVerifyService {
    fun verify(headers: Headers): Boolean
}
