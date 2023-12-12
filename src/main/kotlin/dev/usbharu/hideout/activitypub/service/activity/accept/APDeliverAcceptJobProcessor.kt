package dev.usbharu.hideout.activitypub.service.activity.accept

import dev.usbharu.hideout.activitypub.service.common.APRequestService
import dev.usbharu.hideout.application.external.Transaction
import dev.usbharu.hideout.core.external.job.DeliverAcceptJob
import dev.usbharu.hideout.core.external.job.DeliverAcceptJobParam
import dev.usbharu.hideout.core.query.ActorQueryService
import dev.usbharu.hideout.core.service.job.JobProcessor
import org.springframework.stereotype.Service

@Service
class APDeliverAcceptJobProcessor(
    private val apRequestService: APRequestService,
    private val actorQueryService: ActorQueryService,
    private val deliverAcceptJob: DeliverAcceptJob,
    private val transaction: Transaction
) :
    JobProcessor<DeliverAcceptJobParam, DeliverAcceptJob> {
    override suspend fun process(param: DeliverAcceptJobParam): Unit = transaction.transaction {
        apRequestService.apPost(param.inbox, param.accept, actorQueryService.findById(param.signer))
    }

    override fun job(): DeliverAcceptJob = deliverAcceptJob
}
