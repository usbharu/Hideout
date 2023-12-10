package dev.usbharu.hideout.activitypub.service.activity.reject

import dev.usbharu.hideout.activitypub.service.common.APRequestService
import dev.usbharu.hideout.core.external.job.DeliverRejectJob
import dev.usbharu.hideout.core.external.job.DeliverRejectJobParam
import dev.usbharu.hideout.core.query.UserQueryService
import dev.usbharu.hideout.core.service.job.JobProcessor
import org.springframework.stereotype.Component

@Component
class APDeliverRejectJobProcessor(
    private val apRequestService: APRequestService,
    private val userQueryService: UserQueryService,
    private val deliverRejectJob: DeliverRejectJob
) :
    JobProcessor<DeliverRejectJobParam, DeliverRejectJob> {
    override suspend fun process(param: DeliverRejectJobParam) {
        apRequestService.apPost(param.inbox, param.reject, userQueryService.findById(param.signer))
    }

    override fun job(): DeliverRejectJob = deliverRejectJob
}
