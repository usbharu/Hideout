package dev.usbharu.hideout.core.service.resource

import java.io.InputStream

interface ResolveResponse {
    suspend fun body(): InputStream
    suspend fun header(): Map<String, List<String>>
    suspend fun status(): Int
    suspend fun statusMessage(): String
}
