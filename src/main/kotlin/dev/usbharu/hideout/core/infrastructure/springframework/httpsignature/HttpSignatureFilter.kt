package dev.usbharu.hideout.core.infrastructure.springframework.httpsignature

import dev.usbharu.hideout.activitypub.service.objects.user.APUserService
import dev.usbharu.hideout.application.external.Transaction
import dev.usbharu.hideout.core.domain.exception.FailedToGetResourcesException
import dev.usbharu.hideout.core.query.UserQueryService
import dev.usbharu.httpsignature.common.HttpHeaders
import dev.usbharu.httpsignature.common.HttpMethod
import dev.usbharu.httpsignature.common.HttpRequest
import dev.usbharu.httpsignature.verify.SignatureHeaderParser
import jakarta.servlet.http.HttpServletRequest
import kotlinx.coroutines.runBlocking
import org.springframework.security.web.authentication.preauth.AbstractPreAuthenticatedProcessingFilter
import java.net.URL

class HttpSignatureFilter(
    private val httpSignatureHeaderParser: SignatureHeaderParser,
    private val transaction: Transaction,
    private val apUserService: APUserService,
    private val userQueryService: UserQueryService
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
        runBlocking {
            transaction.transaction {
                try {
                    userQueryService.findByKeyId(signature.keyId)
                } catch (_: FailedToGetResourcesException) {
                    apUserService.fetchPerson(signature.keyId)
                }
            }
        }
        return signature.keyId
    }

    override fun getPreAuthenticatedCredentials(request: HttpServletRequest?): Any {
        requireNotNull(request)
        val url = request.requestURL.toString()

        val headersList = request.headerNames?.toList().orEmpty()

        val headers =
            headersList.associateWith { header -> request.getHeaders(header)?.toList().orEmpty() }

        val method = when (val method = request.method.lowercase()) {
            "get" -> HttpMethod.GET
            "post" -> HttpMethod.POST
            else -> {
                throw IllegalArgumentException("Unsupported method: $method")
            }
        }

        return HttpRequest(
            URL(url + request.queryString.orEmpty()),
            HttpHeaders(headers),
            method
        )
    }
}
