package dev.usbharu.hideout.activitypub.service.tmp

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import dev.usbharu.hideout.activitypub.domain.model.objects.Object
import dev.usbharu.hideout.activitypub.service.common.ActivityType
import dev.usbharu.hideout.activitypub.service.objects.user.APUserService
import dev.usbharu.hideout.core.domain.exception.FailedToGetResourcesException
import dev.usbharu.hideout.core.external.job.InboxJob
import dev.usbharu.hideout.core.query.UserQueryService
import dev.usbharu.hideout.util.RsaUtil
import dev.usbharu.httpsignature.common.HttpHeaders
import dev.usbharu.httpsignature.common.HttpRequest
import dev.usbharu.httpsignature.common.PublicKey
import dev.usbharu.httpsignature.verify.HttpSignatureVerifier
import dev.usbharu.httpsignature.verify.Signature
import dev.usbharu.httpsignature.verify.SignatureHeaderParser
import kjob.core.job.JobProps
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service

@Service
class InboxJobProcessor(
    private val activityPubProcessorList: List<ActivityPubProcessor<Object>>,
    private val objectMapper: ObjectMapper,
    private val signatureHeaderParser: SignatureHeaderParser,
    private val signatureVerifier: HttpSignatureVerifier,
    private val userQueryService: UserQueryService,
    private val apUserService: APUserService
) {
    suspend fun process(props: JobProps<InboxJob>) {

        val type = ActivityType.valueOf(props[InboxJob.type])
        val jsonString = objectMapper.readTree(props[InboxJob.json])
        val httpRequestString = props[InboxJob.httpRequest]
        val headersString = props[InboxJob.headers]

        logger.info("START Process inbox. type: {}", type)
        logger.trace("type: {} \njson: \n{}", type, jsonString.toPrettyString())

        val map = objectMapper.readValue<Map<String, List<String>>>(headersString)

        val httpRequest =
            objectMapper.readValue<HttpRequest>(httpRequestString).copy(headers = HttpHeaders(map))

        logger.trace("request: {}\nheaders: {}", httpRequest, map)

        val signature = parseSignatureHeader(httpRequest.headers)

        logger.debug("Has signature? {}", signature != null)

        val verify = signature?.let { verifyHttpSignature(httpRequest, it) } ?: false

        logger.debug("Is verifying success? {}", verify)

        val activityPubProcessor = activityPubProcessorList.firstOrNull { it.isSupported(type) }

        if (activityPubProcessor == null) {
            logger.warn("ActivityType {} is not support.", type)
            throw IllegalStateException("ActivityPubProcessor not found.")
        }

        val value = objectMapper.treeToValue(jsonString, activityPubProcessor.type())
        activityPubProcessor.process(ActivityPubProcessContext(value, jsonString, httpRequest, signature, verify))

        logger.info("SUCCESS Process inbox. type: {}", type)
    }

    private suspend fun verifyHttpSignature(httpRequest: HttpRequest, signature: Signature): Boolean {
        val user = try {
            userQueryService.findByKeyId(signature.keyId)
        } catch (_: FailedToGetResourcesException) {
            apUserService.fetchPersonWithEntity(signature.keyId).second
        }

        val verify = signatureVerifier.verify(
            httpRequest,
            PublicKey(RsaUtil.decodeRsaPublicKeyPem(user.publicKey), signature.keyId)
        )

        return verify.success
    }

    private fun parseSignatureHeader(httpHeaders: HttpHeaders): Signature? {
        return try {
            signatureHeaderParser.parse(httpHeaders)
        } catch (e: RuntimeException) {
            logger.trace("FAILED parse signature header", e)
            null
        }
    }

    companion object {
        private val logger = LoggerFactory.getLogger(InboxJobProcessor::class.java)
    }
}
