package dev.usbharu.hideout.activitypub.service.activity.like

import dev.usbharu.hideout.core.external.job.DeliverReactionJob
import dev.usbharu.hideout.core.external.job.DeliverRemoveReactionJob
import kjob.core.job.JobProps

interface ApReactionJobService {
    suspend fun reactionJob(props: JobProps<DeliverReactionJob>)
    suspend fun removeReactionJob(props: JobProps<DeliverRemoveReactionJob>)
}
