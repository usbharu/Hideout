package dev.usbharu.hideout.service.ap

import dev.usbharu.hideout.domain.model.hideout.entity.Reaction
import dev.usbharu.hideout.domain.model.job.DeliverReactionJob
import dev.usbharu.hideout.domain.model.job.DeliverRemoveReactionJob
import kjob.core.job.JobProps

interface APReactionService {
    suspend fun reaction(like: Reaction)
    suspend fun removeReaction(like: Reaction)
    suspend fun reactionJob(props: JobProps<DeliverReactionJob>)
    suspend fun removeReactionJob(props: JobProps<DeliverRemoveReactionJob>)
}
