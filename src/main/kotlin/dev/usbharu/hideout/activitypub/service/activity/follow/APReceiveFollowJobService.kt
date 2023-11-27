package dev.usbharu.hideout.activitypub.service.activity.follow

import dev.usbharu.hideout.core.external.job.ReceiveFollowJob
import kjob.core.job.JobProps

@Deprecated("use activitypub processor")
interface APReceiveFollowJobService {
    suspend fun receiveFollowJob(props: JobProps<ReceiveFollowJob>)
}
