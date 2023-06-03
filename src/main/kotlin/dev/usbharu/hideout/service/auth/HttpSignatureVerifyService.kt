package dev.usbharu.hideout.service.auth

import io.ktor.http.*

interface HttpSignatureVerifyService {
    fun verify(headers: Headers): Boolean
}
