package dev.usbharu.hideout.activitypub.service.objects.note

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import dev.usbharu.hideout.activitypub.domain.model.Create
import dev.usbharu.hideout.activitypub.service.common.APRequestService
import dev.usbharu.hideout.application.external.Transaction
import dev.usbharu.hideout.core.external.job.DeliverPostJob
import dev.usbharu.hideout.core.query.UserQueryService
import kjob.core.job.JobProps
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.stereotype.Component

@Component
class ApNoteJobServiceImpl(
    private val userQueryService: UserQueryService,
    private val apRequestService: APRequestService,
    @Qualifier("activitypub") private val objectMapper: ObjectMapper,
    private val transaction: Transaction
) : ApNoteJobService {
    override suspend fun createNoteJob(props: JobProps<DeliverPostJob>) {
        val actor = props[DeliverPostJob.actor]
        val create = objectMapper.readValue<Create>(props[DeliverPostJob.create])
        transaction.transaction {
            val signer = userQueryService.findByUrl(actor)

            val inbox = props[DeliverPostJob.inbox]
            logger.debug("createNoteJob: actor={}, create={}, inbox={}", actor, create, inbox)
            apRequestService.apPost(
                inbox,
                create,
                signer
            )
        }
    }

    companion object {
        private val logger = LoggerFactory.getLogger(ApNoteJobServiceImpl::class.java)
    }
}
