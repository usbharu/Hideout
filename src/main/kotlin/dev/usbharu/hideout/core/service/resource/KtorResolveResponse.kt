package dev.usbharu.hideout.core.service.resource

import io.ktor.client.statement.*
import io.ktor.util.*
import io.ktor.utils.io.jvm.javaio.*
import java.io.InputStream

class KtorResolveResponse(val ktorHttpResponse: HttpResponse) : ResolveResponse {

    override suspend fun body(): InputStream = ktorHttpResponse.bodyAsChannel().toInputStream()
    override suspend fun header(): Map<String, List<String>> = ktorHttpResponse.headers.toMap()
    override suspend fun status(): Int = ktorHttpResponse.status.value
    override suspend fun statusMessage(): String = ktorHttpResponse.status.description
}
