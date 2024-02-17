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
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as KtorResolveResponse

        if (ktorHttpResponse != other.ktorHttpResponse) return false
        if (_bodyAsText != other._bodyAsText) return false
        if (!_bodyAsBytes.contentEquals(other._bodyAsBytes)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = ktorHttpResponse.hashCode()
        result = 31 * result + _bodyAsText.hashCode()
        result = 31 * result + _bodyAsBytes.contentHashCode()
        return result
    }
}
