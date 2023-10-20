package dev.usbharu.hideout.service.ap

import com.fasterxml.jackson.databind.ObjectMapper
import dev.usbharu.hideout.domain.model.ap.Object
import dev.usbharu.hideout.domain.model.hideout.entity.User
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

    override suspend fun <R : Object> apGet(url: String, signer: User?, responseClass: Class<R>): R {
        val date = dateTimeFormatter.format(ZonedDateTime.now(ZoneId.of("GMT")))
        val u = URL(url)
        if (signer?.privateKey == null) {
            val bodyAsText = httpClient.get(url) {
                header("Accept", ContentType.Application.Activity)
                header("Date", date)
            }.bodyAsText()
            return objectMapper.readValue(bodyAsText, responseClass)
        }

        val headers = headers {
            append("Accept", ContentType.Application.Activity)
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
                privateKey = RsaUtil.decodeRsaPrivateKeyPem(signer.privateKey),
            ),
            signHeaders = listOf("(request-target)", "date", "host", "accept")
        )

        val bodyAsText = httpClient.get(url) {
            headers {
                headers {
                    appendAll(headers)
                    append("Signature", sign.signatureHeader)
                    remove("Host")
                }
            }
            contentType(ContentType.Application.Activity)
        }.bodyAsText()
        return objectMapper.readValue(bodyAsText, responseClass)
    }

    override suspend fun <T : Object, R : Object> apPost(
        url: String,
        body: T?,
        signer: User?,
        responseClass: Class<R>
    ): R {
        val bodyAsText = apPost(url, body, signer)
        return objectMapper.readValue(bodyAsText, responseClass)
    }

    override suspend fun <T : Object> apPost(url: String, body: T?, signer: User?): String {
        if (body != null) {
            val mutableListOf = mutableListOf<String>()
            mutableListOf.add("https://www.w3.org/ns/activitystreams")
            mutableListOf.addAll(body.context)
            body.context = mutableListOf
        }

        val requestBody = objectMapper.writeValueAsString(body)

        val sha256 = MessageDigest.getInstance("SHA-256")

        val digest = Base64Util.encode(sha256.digest(requestBody.toByteArray()))

        val date = dateTimeFormatter.format(ZonedDateTime.now(ZoneId.of("GMT")))
        val u = URL(url)
        if (signer?.privateKey == null) {
            logger.debug("NOT SIGN Request: {}", url)
            logger.trace("{}", signer)
            return httpClient.post(url) {
                header("Accept", ContentType.Application.Activity)
                header("Date", date)
                header("Digest", "sha-256=$digest")
                setBody(requestBody)
                contentType(ContentType.Application.Activity)
            }.bodyAsText()
        }

        logger.debug("SIGN Request: {}", url)

        val headers = headers {
            append("Accept", ContentType.Application.Activity)
            append("Date", date)
            append("Host", u.host)
            append("Digest", "sha-256=$digest")
        }

        val sign = httpSignatureSigner.sign(
            httpRequest = HttpRequest(
                u,
                HttpHeaders(headers.toMap()),
                HttpMethod.POST
            ),
            privateKey = PrivateKey(
                keyId = signer.keyId,
                privateKey = RsaUtil.decodeRsaPrivateKeyPem(signer.privateKey)
            ),
            signHeaders = listOf("(request-target)", "date", "host", "digest")
        )

        return httpClient.post(url) {
            headers {
                headers {
                    appendAll(headers)
                    append("Signature", sign.signatureHeader)
                    remove("Host")
                }
            }
            setBody(requestBody)
            contentType(ContentType.Application.Activity)
        }.bodyAsText()
    }

    companion object {
        private val logger = LoggerFactory.getLogger(APRequestServiceImpl::class.java)
    }
}
