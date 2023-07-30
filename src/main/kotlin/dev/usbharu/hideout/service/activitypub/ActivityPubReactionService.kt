package dev.usbharu.hideout.service.activitypub

import dev.usbharu.hideout.domain.model.hideout.entity.Reaction
import dev.usbharu.hideout.domain.model.job.DeliverReactionJob
import kjob.core.job.JobProps

interface ActivityPubReactionService {
    suspend fun reaction(like: Reaction)
    suspend fun reactionJob(props: JobProps<DeliverReactionJob>)
}
