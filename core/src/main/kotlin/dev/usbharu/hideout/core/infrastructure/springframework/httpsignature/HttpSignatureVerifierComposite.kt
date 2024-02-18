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

package dev.usbharu.hideout.core.infrastructure.springframework.httpsignature

import dev.usbharu.httpsignature.common.HttpRequest
import dev.usbharu.httpsignature.common.PublicKey
import dev.usbharu.httpsignature.verify.HttpSignatureVerifier
import dev.usbharu.httpsignature.verify.SignatureHeaderParser
import dev.usbharu.httpsignature.verify.VerificationResult

class HttpSignatureVerifierComposite(
    private val map: Map<String, HttpSignatureVerifier>,
    private val httpSignatureHeaderParser: SignatureHeaderParser
) : HttpSignatureVerifier {
    override fun verify(httpRequest: HttpRequest, key: PublicKey): VerificationResult {
        val signature = httpSignatureHeaderParser.parse(httpRequest.headers)
        val verify = map[signature.algorithm]?.verify(httpRequest, key)
        if (verify != null) {
            return verify
        }

        throw IllegalArgumentException("Unsupported algorithm. ${signature.algorithm}")
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as HttpSignatureVerifierComposite

        if (map != other.map) return false
        if (httpSignatureHeaderParser != other.httpSignatureHeaderParser) return false

        return true
    }

    override fun hashCode(): Int {
        var result = map.hashCode()
        result = 31 * result + httpSignatureHeaderParser.hashCode()
        return result
    }
}
