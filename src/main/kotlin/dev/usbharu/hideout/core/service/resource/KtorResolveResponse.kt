package dev.usbharu.hideout.core.service.resource

import io.ktor.client.statement.*
import io.ktor.util.*
import io.ktor.utils.io.jvm.javaio.*
import java.io.InputStream

class KtorResolveResponse(val ktorHttpResponse: HttpResponse) : ResolveResponse {

    private lateinit var _bodyAsText: String
    private lateinit var _bodyAsBytes: ByteArray

    override suspend fun body(): InputStream = ktorHttpResponse.bodyAsChannel().toInputStream()
    override suspend fun bodyAsText(): String {
        if (!this::_bodyAsText.isInitialized) {
            _bodyAsText = ktorHttpResponse.bodyAsText()
        }
        return _bodyAsText
    }

    override suspend fun bodyAsBytes(): ByteArray {
        if (!this::_bodyAsBytes.isInitialized) {
            _bodyAsBytes = ktorHttpResponse.readBytes()
        }
        return _bodyAsBytes
    }

    override suspend fun header(): Map<String, List<String>> = ktorHttpResponse.headers.toMap()
    override suspend fun status(): Int = ktorHttpResponse.status.value
    override suspend fun statusMessage(): String = ktorHttpResponse.status.description
}
