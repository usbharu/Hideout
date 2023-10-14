package dev.usbharu.hideout.service.signature

import io.ktor.http.*

interface HttpSignatureSigner {
    @Suppress("LongParameterList")
    suspend fun sign(
        url: String,
        method: HttpMethod,
        headers: Headers,
        requestBody: String,
        keyPair: Key,
        signHeaders: List<String>
    ): SignedRequest

    suspend fun signRaw(signString: String, keyPair: Key, signHeaders: List<String>): Sign
}
