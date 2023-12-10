package dev.usbharu.hideout.activitypub.service.activity.block

import dev.usbharu.hideout.activitypub.domain.model.Block
import dev.usbharu.hideout.activitypub.domain.model.Follow
import dev.usbharu.hideout.activitypub.domain.model.Reject
import dev.usbharu.hideout.application.config.ApplicationConfig
import dev.usbharu.hideout.core.domain.model.user.User
import dev.usbharu.hideout.core.external.job.DeliverBlockJob
import dev.usbharu.hideout.core.external.job.DeliverBlockJobParam
import dev.usbharu.hideout.core.service.job.JobQueueParentService
import org.springframework.stereotype.Service

interface APSendBlockService {
    suspend fun sendBlock(user: User, target: User)
}

@Service
class ApSendBlockServiceImpl(
    private val applicationConfig: ApplicationConfig,
    private val jobQueueParentService: JobQueueParentService,
    private val deliverBlockJob: DeliverBlockJob
) : APSendBlockService {
    override suspend fun sendBlock(user: User, target: User) {
        val blockJobParam = DeliverBlockJobParam(
            user.id,
            Block(
                user.url,
                "${applicationConfig.url}/block/${user.id}/${target.id}",
                target.url
            ),
            Reject(
                user.url,
                "${applicationConfig.url}/reject/${user.id}/${target.id}",
                Follow(
                    apObject = user.url,
                    actor = target.url
                )
            ),
            target.inbox
        )
        jobQueueParentService.scheduleTypeSafe(deliverBlockJob, blockJobParam)
    }
}