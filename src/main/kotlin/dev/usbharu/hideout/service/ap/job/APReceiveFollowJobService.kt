package dev.usbharu.hideout.service.ap.job

import dev.usbharu.hideout.domain.model.job.ReceiveFollowJob
import kjob.core.job.JobProps

interface APReceiveFollowJobService {
    suspend fun receiveFollowJob(props: JobProps<ReceiveFollowJob>)
}
