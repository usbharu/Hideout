package dev.usbharu.hideout.core.infrastructure.httpsignature

import com.fasterxml.jackson.annotation.JsonSubTypes
import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.JsonDeserializer
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import dev.usbharu.httpsignature.common.HttpHeaders
import dev.usbharu.httpsignature.common.HttpMethod
import dev.usbharu.httpsignature.common.HttpRequest
import java.net.URL


@JsonDeserialize(using = HttpRequestDeserializer::class)
@JsonSubTypes
abstract class HttpRequestMixIn

class HttpRequestDeserializer : JsonDeserializer<HttpRequest>() {
    override fun deserialize(p: JsonParser, ctxt: DeserializationContext?): HttpRequest {

        val readTree: JsonNode = p.codec.readTree(p)



        return HttpRequest(
            URL(readTree["url"].textValue()),
            HttpHeaders(emptyMap()),
            HttpMethod.valueOf(readTree["method"].textValue())
        )
    }

}
