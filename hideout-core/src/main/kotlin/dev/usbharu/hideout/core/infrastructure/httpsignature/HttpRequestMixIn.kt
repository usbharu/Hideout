/*
 * Copyright (C) 2024 usbharu
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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
@Suppress("UnnecessaryAbstractClass")
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
