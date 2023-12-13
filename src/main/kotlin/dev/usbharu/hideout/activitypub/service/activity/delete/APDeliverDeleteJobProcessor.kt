package dev.usbharu.hideout.activitypub.service.activity.delete

import dev.usbharu.hideout.activitypub.service.common.APRequestService
import dev.usbharu.hideout.application.external.Transaction
import dev.usbharu.hideout.core.external.job.DeliverDeleteJob
import dev.usbharu.hideout.core.external.job.DeliverDeleteJobParam
import dev.usbharu.hideout.core.query.ActorQueryService
import dev.usbharu.hideout.core.service.job.JobProcessor
import org.springframework.stereotype.Service

@Service
class APDeliverDeleteJobProcessor(
    private val apRequestService: APRequestService,
    private val actorQueryService: ActorQueryService,
    private val transaction: Transaction,
    private val deliverDeleteJob: DeliverDeleteJob
) : JobProcessor<DeliverDeleteJobParam, DeliverDeleteJob> {
    override suspend fun process(param: DeliverDeleteJobParam): Unit = transaction.transaction {
        apRequestService.apPost(param.inbox, param.delete, actorQueryService.findById(param.signer))
    }

    override fun job(): DeliverDeleteJob = deliverDeleteJob
}
