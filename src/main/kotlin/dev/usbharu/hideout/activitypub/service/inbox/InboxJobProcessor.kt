package dev.usbharu.hideout.activitypub.service.inbox

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import dev.usbharu.hideout.activitypub.domain.model.objects.Object
import dev.usbharu.hideout.activitypub.service.common.ActivityPubProcessContext
import dev.usbharu.hideout.activitypub.service.common.ActivityPubProcessor
import dev.usbharu.hideout.activitypub.service.objects.user.APUserService
import dev.usbharu.hideout.application.external.Transaction
import dev.usbharu.hideout.core.domain.exception.FailedToGetResourcesException
import dev.usbharu.hideout.core.external.job.InboxJob
import dev.usbharu.hideout.core.external.job.InboxJobParam
import dev.usbharu.hideout.core.query.UserQueryService
import dev.usbharu.hideout.core.service.job.JobProcessor
import dev.usbharu.hideout.util.RsaUtil
import dev.usbharu.httpsignature.common.HttpHeaders
import dev.usbharu.httpsignature.common.HttpMethod
import dev.usbharu.httpsignature.common.HttpRequest
import dev.usbharu.httpsignature.common.PublicKey
import dev.usbharu.httpsignature.verify.HttpSignatureVerifier
import dev.usbharu.httpsignature.verify.Signature
import dev.usbharu.httpsignature.verify.SignatureHeaderParser
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service

@Service
class InboxJobProcessor(
    private val activityPubProcessorList: List<ActivityPubProcessor<*>>,
    private val objectMapper: ObjectMapper,
    private val signatureHeaderParser: SignatureHeaderParser,
    private val signatureVerifier: HttpSignatureVerifier,
    private val userQueryService: UserQueryService,
    private val apUserService: APUserService,
    private val transaction: Transaction
) : JobProcessor<InboxJobParam, InboxJob> {

    private suspend fun verifyHttpSignature(
        httpRequest: HttpRequest,
        signature: Signature,
        transaction: Transaction
    ): Boolean {
        val requiredHeaders = when (httpRequest.method) {
            HttpMethod.GET -> getRequiredHeaders
            HttpMethod.POST -> postRequiredHeaders
        }
        if (signature.headers.containsAll(requiredHeaders).not()) {
            logger.warn("FAILED Invalid signature. require: {}", requiredHeaders)
            return false
        }

        val user = transaction.transaction {
            try {
                userQueryService.findByKeyId(signature.keyId)
            } catch (_: FailedToGetResourcesException) {
                apUserService.fetchPersonWithEntity(signature.keyId).second
            }
        }

        val verify = try {
            signatureVerifier.verify(
                httpRequest,
                PublicKey(RsaUtil.decodeRsaPublicKeyPem(user.publicKey), signature.keyId)
            )
        } catch (e: Exception) {
            logger.warn("FAILED Verify Http Signature", e)
            return false
        }

        return verify.success
    }

    @Suppress("TooGenericExceptionCaught")
    private fun parseSignatureHeader(httpHeaders: HttpHeaders): Signature? {
        return try {
            println("Signature Header =" + httpHeaders.get("Signature").single())
            signatureHeaderParser.parse(httpHeaders)
        } catch (e: RuntimeException) {
            logger.trace("FAILED parse signature header", e)
            null
        }
    }

    override suspend fun process(param: InboxJobParam) {

        val jsonNode = objectMapper.readTree(param.json)

        logger.info("START Process inbox. type: {}", param.type)
        logger.trace("type: {}\njson: \n{}", param.type, jsonNode.toPrettyString())

        val map = objectMapper.readValue<Map<String, List<String>>>(param.headers)

        val httpRequest = objectMapper.readValue<HttpRequest>(param.httpRequest).copy(headers = HttpHeaders(map))

        logger.trace("Request: {}\nheaders: {}", httpRequest, map)

        val signature = parseSignatureHeader(httpRequest.headers)

        logger.debug("Has signature? {}", signature != null)

        val verify = signature?.let { verifyHttpSignature(httpRequest, it, transaction) } ?: false

        transaction.transaction {
            logger.debug("Is verifying success? {}", verify)

            val activityPubProcessor =
                activityPubProcessorList.firstOrNull { it.isSupported(param.type) } as ActivityPubProcessor<Object>?

            if (activityPubProcessor == null) {
                logger.warn("ActivityType {} is not support.", param.type)
                throw IllegalStateException("ActivityPubProcessor not found.")
            }

            val value = objectMapper.treeToValue(jsonNode, activityPubProcessor.type())
            activityPubProcessor.process(ActivityPubProcessContext(value, jsonNode, httpRequest, signature, verify))

            logger.info("SUCCESS Process inbox. type: {}", param.type)
        }
    }

    override fun job(): InboxJob = InboxJob

    companion object {
        private val logger = LoggerFactory.getLogger(InboxJobProcessor::class.java)
        private val postRequiredHeaders = listOf("(request-target)", "date", "host", "digest")
        private val getRequiredHeaders = listOf("(request-target)", "date", "host")
    }
}
