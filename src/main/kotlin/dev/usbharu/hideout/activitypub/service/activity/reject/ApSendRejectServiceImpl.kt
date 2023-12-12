package dev.usbharu.hideout.activitypub.service.activity.reject

import dev.usbharu.hideout.activitypub.domain.model.Follow
import dev.usbharu.hideout.activitypub.domain.model.Reject
import dev.usbharu.hideout.application.config.ApplicationConfig
import dev.usbharu.hideout.core.domain.model.actor.Actor
import dev.usbharu.hideout.core.external.job.DeliverRejectJob
import dev.usbharu.hideout.core.external.job.DeliverRejectJobParam
import dev.usbharu.hideout.core.service.job.JobQueueParentService
import org.springframework.stereotype.Service

@Service
class ApSendRejectServiceImpl(
    private val applicationConfig: ApplicationConfig,
    private val jobQueueParentService: JobQueueParentService,
    private val deliverRejectJob: DeliverRejectJob
) : ApSendRejectService {
    override suspend fun sendRejectFollow(actor: Actor, target: Actor) {
        val deliverRejectJobParam = DeliverRejectJobParam(
            Reject(
                actor.url,
                "${applicationConfig.url}/reject/${actor.id}/${target.id}",
                Follow(apObject = actor.url, actor = target.url)
            ),
            target.inbox,
            actor.id
        )

        jobQueueParentService.scheduleTypeSafe(deliverRejectJob, deliverRejectJobParam)
    }
}
