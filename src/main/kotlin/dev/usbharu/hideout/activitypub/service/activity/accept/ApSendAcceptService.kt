package dev.usbharu.hideout.activitypub.service.activity.accept

import dev.usbharu.hideout.activitypub.domain.model.Accept
import dev.usbharu.hideout.activitypub.domain.model.Follow
import dev.usbharu.hideout.core.domain.model.user.User
import dev.usbharu.hideout.core.external.job.DeliverAcceptJob
import dev.usbharu.hideout.core.external.job.DeliverAcceptJobParam
import dev.usbharu.hideout.core.service.job.JobQueueParentService
import org.springframework.stereotype.Service

interface ApSendAcceptService {
    suspend fun sendAcceptFollow(user: User, target: User)
}

@Service
class ApSendAcceptServiceImpl(
    private val jobQueueParentService: JobQueueParentService,
    private val deliverAcceptJob: DeliverAcceptJob
) : ApSendAcceptService {
    override suspend fun sendAcceptFollow(user: User, target: User) {
        val deliverAcceptJobParam = DeliverAcceptJobParam(
            Accept(
                apObject = Follow(
                    apObject = user.url,
                    actor = target.url
                ),
                actor = user.url
            ),
            target.inbox,
            user.id
        )

        jobQueueParentService.scheduleTypeSafe(deliverAcceptJob, deliverAcceptJobParam)
    }
}
