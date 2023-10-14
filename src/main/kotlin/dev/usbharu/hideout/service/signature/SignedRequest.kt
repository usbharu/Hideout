package dev.usbharu.hideout.service.signature

import io.ktor.client.request.*
import io.ktor.http.*

data class SignedRequest(
    val url: String,
    val method: HttpMethod,
    val headers: Headers,
    val requestBody: String,
    val sign: Sign
) {
    fun toRequestBuilder(): HttpRequestBuilder {
        val httpRequestBuilder = HttpRequestBuilder()
        httpRequestBuilder.url(this.url)
        httpRequestBuilder.method = this.method
        httpRequestBuilder.headers {
            this.appendAll(headers)
        }
        httpRequestBuilder.setBody(requestBody)
        return httpRequestBuilder
    }
}
