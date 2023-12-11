package dev.usbharu.hideout.activitypub.service.activity.undo

import dev.usbharu.hideout.activitypub.domain.model.Block
import dev.usbharu.hideout.activitypub.domain.model.Follow
import dev.usbharu.hideout.activitypub.domain.model.Undo
import dev.usbharu.hideout.application.config.ApplicationConfig
import dev.usbharu.hideout.core.domain.model.user.User
import dev.usbharu.hideout.core.external.job.DeliverUndoJob
import dev.usbharu.hideout.core.external.job.DeliverUndoJobParam
import dev.usbharu.hideout.core.service.job.JobQueueParentService
import org.springframework.stereotype.Service
import java.time.Instant

@Service
class APSendUndoServiceImpl(
    private val jobQueueParentService: JobQueueParentService,
    private val deliverUndoJob: DeliverUndoJob,
    private val applicationConfig: ApplicationConfig
) : APSendUndoService {
    override suspend fun sendUndoFollow(user: User, target: User) {
        val deliverUndoJobParam = DeliverUndoJobParam(
            Undo(
                actor = user.url,
                id = "${applicationConfig.url}/undo/follow/${user.id}/${target.url}",
                apObject = Follow(
                    apObject = user.url,
                    actor = target.url
                ),
                published = Instant.now().toString()
            ),
            target.inbox,
            user.id
        )

        jobQueueParentService.scheduleTypeSafe(deliverUndoJob, deliverUndoJobParam)
    }

    override suspend fun sendUndoBlock(user: User, target: User) {
        val deliverUndoJobParam = DeliverUndoJobParam(
            Undo(
                actor = user.url,
                id = "${applicationConfig.url}/undo/block/${user.id}/${target.url}",
                apObject = Block(
                    apObject = user.url,
                    actor = target.url,
                    id = "${applicationConfig.url}/block/${user.id}/${target.id}"
                ),
                published = Instant.now().toString()
            ),
            target.inbox,
            user.id
        )

        jobQueueParentService.scheduleTypeSafe(deliverUndoJob, deliverUndoJobParam)
    }
}
