package dev.usbharu.hideout.activitypub.service.common

import com.fasterxml.jackson.databind.ObjectMapper
import dev.usbharu.hideout.activitypub.domain.model.objects.Object
import dev.usbharu.hideout.core.domain.model.actor.Actor
import dev.usbharu.hideout.util.Base64Util
import dev.usbharu.hideout.util.HttpUtil.Activity
import dev.usbharu.hideout.util.RsaUtil
import dev.usbharu.httpsignature.common.HttpHeaders
import dev.usbharu.httpsignature.common.HttpMethod
import dev.usbharu.httpsignature.common.HttpRequest
import dev.usbharu.httpsignature.common.PrivateKey
import dev.usbharu.httpsignature.sign.HttpSignatureSigner
import io.ktor.client.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.util.*
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.stereotype.Service
import java.net.URL
import java.security.MessageDigest
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

@Service
class APRequestServiceImpl(
    private val httpClient: HttpClient,
    @Qualifier("activitypub") private val objectMapper: ObjectMapper,
    private val httpSignatureSigner: HttpSignatureSigner,
    @Qualifier("http") private val dateTimeFormatter: DateTimeFormatter,
) : APRequestService {

    override suspend fun <R : Object> apGet(url: String, signer: Actor?, responseClass: Class<R>): R {
        logger.debug("START ActivityPub Request GET url: {}, signer: {}", url, signer?.url)
        val date = dateTimeFormatter.format(ZonedDateTime.now(ZoneId.of("GMT")))
        val u = URL(url)
        val httpResponse = if (signer?.privateKey == null) {
            apGetNotSign(url, date)
        } else {
            apGetSign(date, u, signer, url)
        }

        val bodyAsText = httpResponse.bodyAsText()
        val readValue = objectMapper.readValue(bodyAsText, responseClass)
        logger.debug(
            "SUCCESS ActivityPub Request GET status: {} url: {}",
            httpResponse.status,
            httpResponse.request.url
        )
        logBody(bodyAsText, url)
        return readValue
    }

    private suspend fun apGetSign(
        date: String,
        u: URL,
        signer: Actor,
        url: String
    ): HttpResponse {
        val headers = headers {
            append("Accept", Activity)
            append("Date", date)
            append("Host", u.host)
        }

        val sign = httpSignatureSigner.sign(
            httpRequest = HttpRequest(
                url = u,
                headers = HttpHeaders(headers.toMap()),
                HttpMethod.GET
            ),
            privateKey = PrivateKey(
                keyId = "${signer.url}#pubkey",
                privateKey = RsaUtil.decodeRsaPrivateKeyPem(signer.privateKey!!),
            ),
            signHeaders = listOf("(request-target)", "date", "host", "accept")
        )

        val httpResponse = httpClient.get(url) {
            headers {
                headers {
                    appendAll(headers)
                    append("Signature", sign.signatureHeader)
//                    remove("Host")
                }
            }
            contentType(Activity)
        }
        return httpResponse
    }

    private suspend fun apGetNotSign(url: String, date: String?) = httpClient.get(url) {
        header("Accept", Activity)
        header("Date", date)
    }

    override suspend fun <T : Object, R : Object> apPost(
        url: String,
        body: T?,
        signer: Actor?,
        responseClass: Class<R>
    ): R {
        val bodyAsText = apPost(url, body, signer)
        return objectMapper.readValue(bodyAsText, responseClass)
    }

    override suspend fun <T : Object> apPost(url: String, body: T?, signer: Actor?): String {
        logger.debug("START ActivityPub Request POST url: {}, signer: {}", url, signer?.url)
        val requestBody = addContextIfNotNull(body)

        logger.trace(
            """
                |
            |***** BEGIN HTTP Request Trace url: {} *****
            |
            |$requestBody
            |
            |***** END HTTP Request Trace url: {} *****
            |
            """.trimMargin(),
            url,
            url
        )

        val sha256 = MessageDigest.getInstance("SHA-256")

        val digest = Base64Util.encode(sha256.digest(requestBody.orEmpty().toByteArray()))

        val date = dateTimeFormatter.format(ZonedDateTime.now(ZoneId.of("GMT")))
        val u = URL(url)
        val httpResponse = if (signer?.privateKey == null) {
            apPostNotSign(url, date, digest, requestBody)
        } else {
            apPostSign(date, u, digest, signer, requestBody)
        }

        val bodyAsText = httpResponse.bodyAsText()
        logger.debug(
            "SUCCESS ActivityPub Request POST status: {} url: {}",
            httpResponse.status,
            httpResponse.request.url
        )
        logBody(bodyAsText, url)
        return bodyAsText
    }

    private suspend fun apPostNotSign(
        url: String,
        date: String?,
        digest: String,
        requestBody: String?
    ) = httpClient.post(url) {
        accept(Activity)
        header("Date", date)
        header("Digest", "sha-256=$digest")
        if (requestBody != null) {
            setBody(requestBody)
            contentType(Activity)
        }
    }

    private suspend fun apPostSign(
        date: String,
        u: URL,
        digest: String,
        signer: Actor,
        requestBody: String?
    ): HttpResponse {
        val headers = headers {
            append("Accept", Activity)
            append("Date", date)
            append("Host", u.host)
            append("Digest", "SHA-256=$digest")
        }

        val sign = httpSignatureSigner.sign(
            httpRequest = HttpRequest(
                u,
                HttpHeaders(headers.toMap()),
                HttpMethod.POST
            ),
            privateKey = PrivateKey(
                keyId = signer.keyId,
                privateKey = RsaUtil.decodeRsaPrivateKeyPem(signer.privateKey!!)
            ),
            signHeaders = listOf("(request-target)", "date", "host", "digest")
        )

        val httpResponse = httpClient.post(u) {
            headers {
                appendAll(headers)
                append("Signature", sign.signatureHeader)
//                remove("Host")
            }
            setBody(requestBody)
            contentType(Activity)
        }
        return httpResponse
    }

    private fun <T : Object> addContextIfNotNull(body: T?) = if (body != null) {
        val mutableListOf = mutableListOf<String>()
        mutableListOf.add("https://www.w3.org/ns/activitystreams")
        mutableListOf.addAll(body.context)
        body.context = mutableListOf
        objectMapper.writeValueAsString(body)
    } else {
        null
    }

    private fun logBody(bodyAsText: String, url: String) {
        logger.trace(
            """
                |
                |***** BEGIN HTTP Response Trace url: {} *****
                |
                |$bodyAsText
                |
                |***** END HTTP Response TRACE url: {} *****
                |
            """.trimMargin(),
            url,
            url
        )
    }

    companion object {
        private val logger = LoggerFactory.getLogger(APRequestServiceImpl::class.java)
    }
}
