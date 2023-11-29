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
        if (headersList.contains("Signature").not()) {
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
