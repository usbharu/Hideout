package dev.usbharu.hideout.activitypub.service.activity.undo

import dev.usbharu.hideout.activitypub.service.common.APRequestService
import dev.usbharu.hideout.core.external.job.DeliverUndoJob
import dev.usbharu.hideout.core.external.job.DeliverUndoJobParam
import dev.usbharu.hideout.core.query.UserQueryService
import dev.usbharu.hideout.core.service.job.JobProcessor
import org.springframework.stereotype.Service

@Service
class APDeliverUndoJobProcessor(
    private val deliverUndoJob: DeliverUndoJob,
    private val apRequestService: APRequestService,
    private val userQueryService: UserQueryService
) : JobProcessor<DeliverUndoJobParam, DeliverUndoJob> {
    override suspend fun process(param: DeliverUndoJobParam) {
        apRequestService.apPost(param.inbox, param.undo, userQueryService.findById(param.signer))
    }

    override fun job(): DeliverUndoJob = deliverUndoJob
}
