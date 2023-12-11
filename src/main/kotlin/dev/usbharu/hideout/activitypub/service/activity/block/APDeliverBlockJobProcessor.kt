package dev.usbharu.hideout.activitypub.service.activity.block

import dev.usbharu.hideout.activitypub.service.common.APRequestService
import dev.usbharu.hideout.application.external.Transaction
import dev.usbharu.hideout.core.domain.model.actor.ActorRepository
import dev.usbharu.hideout.core.external.job.DeliverBlockJob
import dev.usbharu.hideout.core.external.job.DeliverBlockJobParam
import dev.usbharu.hideout.core.service.job.JobProcessor
import org.springframework.stereotype.Service

/**
 * ブロックアクティビティ配送を処理します
 */
@Service
class APDeliverBlockJobProcessor(
    private val apRequestService: APRequestService,
    private val actorRepository: ActorRepository,
    private val transaction: Transaction,
    private val deliverBlockJob: DeliverBlockJob
) : JobProcessor<DeliverBlockJobParam, DeliverBlockJob> {
    override suspend fun process(param: DeliverBlockJobParam): Unit = transaction.transaction {
        val signer = actorRepository.findById(param.signer)
        apRequestService.apPost(
            param.inbox,
            param.reject,
            signer
        )
        apRequestService.apPost(
            param.inbox,
            param.block,
            signer
        )
    }

    override fun job(): DeliverBlockJob = deliverBlockJob
}
