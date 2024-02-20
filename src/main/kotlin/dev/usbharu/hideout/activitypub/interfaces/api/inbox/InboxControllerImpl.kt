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

package dev.usbharu.hideout.activitypub.interfaces.api.inbox

import dev.usbharu.hideout.activitypub.service.common.APService
import dev.usbharu.hideout.core.infrastructure.springframework.httpsignature.HttpSignatureHeaderChecker
import dev.usbharu.httpsignature.common.HttpHeaders
import dev.usbharu.httpsignature.common.HttpMethod
import dev.usbharu.httpsignature.common.HttpRequest
import jakarta.servlet.http.HttpServletRequest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.slf4j.MDCContext
import kotlinx.coroutines.withContext
import org.slf4j.LoggerFactory
import org.springframework.http.HttpHeaders.WWW_AUTHENTICATE
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.RestController
import java.net.URL

@RestController
class InboxControllerImpl(
    private val apService: APService,
    private val httpSignatureHeaderChecker: HttpSignatureHeaderChecker,
) : InboxController {
    @Suppress("TooGenericExceptionCaught")
    override suspend fun inbox(
        httpServletRequest: HttpServletRequest,
    ): ResponseEntity<String> {

        val headersList = httpServletRequest.headerNames?.toList().orEmpty()
        LOGGER.trace("Inbox Headers {}", headersList)

        val body = withContext(Dispatchers.IO + MDCContext()) {
            httpServletRequest.inputStream.readAllBytes()!!
        }

        try {
            httpSignatureHeaderChecker.checkDate(httpServletRequest.getHeader("date")!!)
        } catch (e: NullPointerException) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Required date header")
        } catch (e: IllegalArgumentException) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Request is too old.")
        }
        try {
            httpSignatureHeaderChecker.checkHost(httpServletRequest.getHeader("host")!!)
        } catch (e: NullPointerException) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Required host header")
        } catch (e: IllegalArgumentException) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Wrong host for request")
        }
        try {
            httpSignatureHeaderChecker.checkDigest(body, httpServletRequest.getHeader("digest")!!)
        } catch (e: NullPointerException) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body("Required request body digest in digest header (sha256)")
        } catch (e: IllegalArgumentException) {
            return ResponseEntity
                .status(HttpStatus.UNAUTHORIZED)
                .body("Wrong digest for request")
        }

        if (httpServletRequest.getHeader("signature").orEmpty().isBlank()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .header(
                    WWW_AUTHENTICATE,
                    "Signature realm=\"Example\",headers=\"(request-target) date host digest\""
                )
                .build()
        }

        val parseActivity = try {
            apService.parseActivity(body.decodeToString())
        } catch (e: Exception) {
            LOGGER.warn("FAILED Parse Activity", e)
            return ResponseEntity.accepted().build()
        }
        LOGGER.info("INBOX Processing Activity Type: {}", parseActivity)
        try {
            val url = httpServletRequest.requestURL.toString()

            val headers =
                headersList.associateWith { header ->
                    httpServletRequest.getHeaders(header)?.toList().orEmpty()
                }

            apService.processActivity(
                body.decodeToString(),
                parseActivity,
                HttpRequest(
                    URL(url + httpServletRequest.queryString.orEmpty()),
                    HttpHeaders(headers),
                    HttpMethod.GET
                ),
                headers
            )
        } catch (e: Exception) {
            LOGGER.warn("FAILED Process Activity $parseActivity", e)
            return ResponseEntity(HttpStatus.ACCEPTED)
        }
        LOGGER.info("SUCCESS Processing Activity Type: {}", parseActivity)
        return ResponseEntity(HttpStatus.ACCEPTED)
    }

    companion object {
        private val LOGGER = LoggerFactory.getLogger(InboxControllerImpl::class.java)
    }
}
