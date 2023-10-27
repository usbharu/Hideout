package dev.usbharu.hideout.service.ap.job

import dev.usbharu.hideout.domain.model.job.DeliverReactionJob
import dev.usbharu.hideout.domain.model.job.DeliverRemoveReactionJob
import kjob.core.job.JobProps

interface ApReactionJobService {
    suspend fun reactionJob(props: JobProps<DeliverReactionJob>)
    suspend fun removeReactionJob(props: JobProps<DeliverRemoveReactionJob>)
}
