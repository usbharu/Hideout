package dev.usbharu.hideout.activitypub.service.activity.reject

import dev.usbharu.hideout.activitypub.domain.model.Follow
import dev.usbharu.hideout.activitypub.domain.model.Reject
import dev.usbharu.hideout.application.config.ApplicationConfig
import dev.usbharu.hideout.core.domain.model.user.User
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
    override suspend fun sendRejectFollow(user: User, target: User) {

        val deliverRejectJobParam = DeliverRejectJobParam(
            Reject(
                user.url,
                "${applicationConfig.url}/reject/${user.id}/${target.id}",
                Follow(apObject = user.url, actor = target.url)
            ),
            target.inbox,
            user.id
        )

        jobQueueParentService.scheduleTypeSafe(deliverRejectJob, deliverRejectJobParam)
    }
}
