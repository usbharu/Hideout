package dev.usbharu.hideout.activitypub.service.objects.note

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import dev.usbharu.hideout.activitypub.domain.model.Create
import dev.usbharu.hideout.activitypub.service.common.APRequestService
import dev.usbharu.hideout.application.external.Transaction
import dev.usbharu.hideout.core.external.job.DeliverPostJob
import dev.usbharu.hideout.core.external.job.DeliverPostJobParam
import dev.usbharu.hideout.core.query.UserQueryService
import dev.usbharu.hideout.core.service.job.JobProcessor
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service

@Service
class ApNoteJobProcessor(
    private val transaction: Transaction,
    private val objectMapper: ObjectMapper,
    private val userQueryService: UserQueryService,
    private val apRequestService: APRequestService
) : JobProcessor<DeliverPostJobParam, DeliverPostJob> {
    override suspend fun process(param: DeliverPostJobParam) {
        val create = objectMapper.readValue<Create>(param.create)
        transaction.transaction {
            val signer = userQueryService.findByUrl(param.actor)

            logger.debug("CreateNoteJob: actor: {} create: {} inbox: {}", param.actor, create, param.inbox)

            apRequestService.apPost(
                param.inbox,
                create,
                signer
            )
        }
    }

    override fun job(): DeliverPostJob = DeliverPostJob

    companion object {
        private val logger = LoggerFactory.getLogger(ApNoteJobProcessor::class.java)
    }
}
