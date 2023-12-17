package dev.usbharu.hideout.activitypub.service.activity.accept

import dev.usbharu.hideout.activitypub.service.common.APRequestService
import dev.usbharu.hideout.application.external.Transaction
import dev.usbharu.hideout.core.domain.model.actor.ActorRepository
import dev.usbharu.hideout.core.external.job.DeliverAcceptJob
import dev.usbharu.hideout.core.external.job.DeliverAcceptJobParam
import dev.usbharu.hideout.core.service.job.JobProcessor
import org.springframework.stereotype.Service

@Service
class APDeliverAcceptJobProcessor(
    private val apRequestService: APRequestService,
    private val deliverAcceptJob: DeliverAcceptJob,
    private val transaction: Transaction,
    private val actorRepository: ActorRepository
) :
    JobProcessor<DeliverAcceptJobParam, DeliverAcceptJob> {
    override suspend fun process(param: DeliverAcceptJobParam): Unit = transaction.transaction {
        apRequestService.apPost(param.inbox, param.accept, actorRepository.findById(param.signer))
    }

    override fun job(): DeliverAcceptJob = deliverAcceptJob
}
