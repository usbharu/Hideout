package dev.usbharu.hideout.activitypub.service.activity.block

import dev.usbharu.hideout.activitypub.domain.model.Block
import dev.usbharu.hideout.activitypub.domain.model.Follow
import dev.usbharu.hideout.activitypub.domain.model.Reject
import dev.usbharu.hideout.application.config.ApplicationConfig
import dev.usbharu.hideout.core.domain.model.actor.Actor
import dev.usbharu.hideout.core.external.job.DeliverBlockJob
import dev.usbharu.hideout.core.external.job.DeliverBlockJobParam
import dev.usbharu.hideout.core.service.job.JobQueueParentService
import org.springframework.stereotype.Service

interface APSendBlockService {
    suspend fun sendBlock(actor: Actor, target: Actor)
}

@Service
class ApSendBlockServiceImpl(
    private val applicationConfig: ApplicationConfig,
    private val jobQueueParentService: JobQueueParentService,
    private val deliverBlockJob: DeliverBlockJob
) : APSendBlockService {
    override suspend fun sendBlock(actor: Actor, target: Actor) {
        val blockJobParam = DeliverBlockJobParam(
            actor.id,
            Block(
                actor.url,
                "${applicationConfig.url}/block/${actor.id}/${target.id}",
                target.url
            ),
            Reject(
                actor.url,
                "${applicationConfig.url}/reject/${actor.id}/${target.id}",
                Follow(
                    apObject = actor.url,
                    actor = target.url
                )
            ),
            target.inbox
        )
        jobQueueParentService.scheduleTypeSafe(deliverBlockJob, blockJobParam)
    }
}
