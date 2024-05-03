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

import dev.usbharu.httpsignature.common.HttpHeaders
import dev.usbharu.httpsignature.common.HttpMethod
import dev.usbharu.httpsignature.common.HttpRequest
import dev.usbharu.httpsignature.verify.SignatureHeaderParser
import jakarta.servlet.http.HttpServletRequest
import org.springframework.security.web.authentication.preauth.AbstractPreAuthenticatedProcessingFilter
import java.net.URL

class HttpSignatureFilter(
    private val httpSignatureHeaderParser: SignatureHeaderParser,
    private val httpSignatureHeaderChecker: HttpSignatureHeaderChecker,
) :
    AbstractPreAuthenticatedProcessingFilter() {
    override fun getPreAuthenticatedPrincipal(request: HttpServletRequest?): Any? {
        val headersList = request?.headerNames?.toList().orEmpty()

        val headers =
            headersList.associateWith { header -> request?.getHeaders(header)?.toList().orEmpty() }

        val signature = try {
            httpSignatureHeaderParser.parse(HttpHeaders(headers))
        } catch (_: IllegalArgumentException) {
            return null
        } catch (_: RuntimeException) {
            return ""
        }
        return signature.keyId
    }

    override fun getPreAuthenticatedCredentials(request: HttpServletRequest?): Any? {
        requireNotNull(request)
        val url = request.requestURL.toString()

        val headersList = request.headerNames?.toList().orEmpty()

        val headers =
            headersList.associateWith { header -> request.getHeaders(header)?.toList().orEmpty() }

        val method = when (val method = request.method.lowercase()) {
            "get" -> HttpMethod.GET
            "post" -> HttpMethod.POST
            else -> {
//                throw IllegalArgumentException("Unsupported method: $method")
                return null
            }
        }

        try {
            httpSignatureHeaderChecker.checkDate(request.getHeader("date")!!)
            httpSignatureHeaderChecker.checkHost(request.getHeader("host")!!)
            if (request.method.equals("post", true)) {
                httpSignatureHeaderChecker.checkDigest(
                    request.inputStream.readAllBytes()!!,
                    request.getHeader("digest")!!
                )
            }
        } catch (_: NullPointerException) {
            return null
        } catch (_: IllegalArgumentException) {
            return null
        }

        return HttpRequest(
            URL(url + request.queryString.orEmpty()),
            HttpHeaders(headers),
            method
        )
    }
}
