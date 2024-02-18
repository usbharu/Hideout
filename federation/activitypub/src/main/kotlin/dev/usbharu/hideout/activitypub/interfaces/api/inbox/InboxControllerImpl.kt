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
import dev.usbharu.httpsignature.common.HttpHeaders
import dev.usbharu.httpsignature.common.HttpMethod
import dev.usbharu.httpsignature.common.HttpRequest
import org.slf4j.LoggerFactory
import org.springframework.http.HttpHeaders.WWW_AUTHENTICATE
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.context.request.RequestContextHolder
import org.springframework.web.context.request.ServletRequestAttributes
import java.net.URL

@RestController
class InboxControllerImpl(private val apService: APService) : InboxController {
    @Suppress("TooGenericExceptionCaught")
    override suspend fun inbox(
        @RequestBody string: String
    ): ResponseEntity<Unit> {
        val request = (requireNotNull(RequestContextHolder.getRequestAttributes()) as ServletRequestAttributes).request

        val headersList = request.headerNames?.toList().orEmpty()
        LOGGER.trace("Inbox Headers {}", headersList)

        if (headersList.map { it.lowercase() }.contains("signature").not()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .header(
                    WWW_AUTHENTICATE,
                    "Signature realm=\"Example\",headers=\"(request-target) date host digest\""
                )
                .build()
        }

        val parseActivity = try {
            apService.parseActivity(string)
        } catch (e: Exception) {
            LOGGER.warn("FAILED Parse Activity", e)
            return ResponseEntity.accepted().build()
        }
        LOGGER.info("INBOX Processing Activity Type: {}", parseActivity)
        try {
            val url = request.requestURL.toString()

            val headers =
                headersList.associateWith { header -> request.getHeaders(header)?.toList().orEmpty() }

            val method = when (val method = request.method.lowercase()) {
                "get" -> HttpMethod.GET
                "post" -> HttpMethod.POST
                else -> {
                    throw IllegalArgumentException("Unsupported method: $method")
                }
            }

            apService.processActivity(
                string,
                parseActivity,
                HttpRequest(
                    URL(url + request.queryString.orEmpty()),
                    HttpHeaders(headers),
                    method
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
