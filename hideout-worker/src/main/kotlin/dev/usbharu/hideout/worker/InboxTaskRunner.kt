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

package dev.usbharu.hideout.worker

import com.fasterxml.jackson.core.JsonParseException
import com.fasterxml.jackson.databind.ObjectMapper
import dev.usbharu.hideout.activitypub.service.common.ActivityPubProcessContext
import dev.usbharu.hideout.activitypub.service.common.ActivityPubProcessor
import dev.usbharu.hideout.activitypub.service.objects.user.APUserService
import dev.usbharu.hideout.core.application.shared.Transaction
import dev.usbharu.hideout.core.external.job.InboxTask
import dev.usbharu.hideout.core.external.job.InboxTaskDef
import dev.usbharu.hideout.util.RsaUtil
import dev.usbharu.httpsignature.common.HttpHeaders
import dev.usbharu.httpsignature.common.HttpMethod
import dev.usbharu.httpsignature.common.HttpRequest
import dev.usbharu.httpsignature.common.PublicKey
import dev.usbharu.httpsignature.verify.HttpSignatureVerifier
import dev.usbharu.httpsignature.verify.Signature
import dev.usbharu.httpsignature.verify.SignatureHeaderParser
import dev.usbharu.owl.consumer.AbstractTaskRunner
import dev.usbharu.owl.consumer.TaskRequest
import dev.usbharu.owl.consumer.TaskResult
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component

@Component
class InboxTaskRunner(
    private val activityPubProcessorList: List<ActivityPubProcessor<*>>,
    private val signatureHeaderParser: SignatureHeaderParser,
    private val signatureVerifier: HttpSignatureVerifier,
    private val apUserService: APUserService,
    private val objectMapper: ObjectMapper,
    private val transaction: Transaction,
) : AbstractTaskRunner<InboxTask, InboxTaskDef>(InboxTaskDef) {

    @Value("\${hideout.debug.trace-inbox:false}")
    private var traceJson: Boolean = false

    override suspend fun typedRun(typedParam: InboxTask, taskRequest: TaskRequest): TaskResult {
        val jsonNode = objectMapper.readTree(typedParam.json)

        logger.info("START Process inbox. type: {}", typedParam.type)
        if (traceJson) {
            logger.trace("type: {}\njson: \n{}", typedParam.type, jsonNode.toPrettyString())
        }

        val map = typedParam.headers

        val httpRequest = typedParam.httpRequest.copy(headers = HttpHeaders(map))

        logger.trace("Request: {}\nheaders: {}", httpRequest, map)

        val signature = parseSignatureHeader(httpRequest.headers)

        logger.debug("Has signature? {}", signature != null)

        // todo 不正なactorを取得してしまわないようにする
        val verify =
            signature?.let {
                verifyHttpSignature(
                    httpRequest,
                    it,
                    transaction,
                    jsonNode.get("actor")?.asText() ?: signature.keyId
                )
            }
                ?: false

        logger.debug("Is verifying success? {}", verify)

        val activityPubProcessor =
            activityPubProcessorList.firstOrNull { it.isSupported(typedParam.type) } as? ActivityPubProcessor<Object>

        if (activityPubProcessor == null) {
            logger.warn("ActivityType {} is not support.", typedParam.type)
            throw IllegalStateException("ActivityPubProcessor not found. type: ${typedParam.type}")
        }

        val value = try {
            objectMapper.treeToValue(jsonNode, activityPubProcessor.type())
        } catch (e: JsonParseException) {
            logger.warn("Invalid JSON\n\n{}\n\n", jsonNode.toPrettyString())
            throw e
        }
        activityPubProcessor.process(ActivityPubProcessContext(value, jsonNode, httpRequest, signature, verify))

        logger.info("SUCCESS Process inbox. type: {}", typedParam.type)


        return TaskResult.ok()
    }

    private suspend fun verifyHttpSignature(
        httpRequest: HttpRequest,
        signature: Signature,
        transaction: Transaction,
        actor: String,
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
            apUserService.fetchPersonWithEntity(actor).second
        }

        @Suppress("TooGenericExceptionCaught")
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

    companion object {
        private val logger = LoggerFactory.getLogger(InboxTaskRunner::class.java)
        private val postRequiredHeaders = listOf("(request-target)", "date", "host", "digest")
        private val getRequiredHeaders = listOf("(request-target)", "date", "host")
    }
}