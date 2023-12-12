package dev.usbharu.hideout.activitypub.service.activity.accept

import dev.usbharu.hideout.activitypub.domain.model.Accept
import dev.usbharu.hideout.activitypub.domain.model.Follow
import dev.usbharu.hideout.core.domain.model.actor.Actor
import dev.usbharu.hideout.core.external.job.DeliverAcceptJob
import dev.usbharu.hideout.core.external.job.DeliverAcceptJobParam
import dev.usbharu.hideout.core.service.job.JobQueueParentService
import org.springframework.stereotype.Service

interface ApSendAcceptService {
    suspend fun sendAcceptFollow(actor: Actor, target: Actor)
}

@Service
class ApSendAcceptServiceImpl(
    private val jobQueueParentService: JobQueueParentService,
    private val deliverAcceptJob: DeliverAcceptJob
) : ApSendAcceptService {
    override suspend fun sendAcceptFollow(actor: Actor, target: Actor) {
        val deliverAcceptJobParam = DeliverAcceptJobParam(
            Accept(
                apObject = Follow(
                    apObject = actor.url,
                    actor = target.url
                ),
                actor = actor.url
            ),
            target.inbox,
            actor.id
        )

        jobQueueParentService.scheduleTypeSafe(deliverAcceptJob, deliverAcceptJobParam)
    }
}
