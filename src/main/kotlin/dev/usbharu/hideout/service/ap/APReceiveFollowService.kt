package dev.usbharu.hideout.service.ap

import dev.usbharu.hideout.domain.model.ActivityPubResponse
import dev.usbharu.hideout.domain.model.ap.Follow
import dev.usbharu.hideout.domain.model.job.ReceiveFollowJob
import kjob.core.job.JobProps

interface APReceiveFollowService {
    suspend fun receiveFollow(follow: Follow): ActivityPubResponse
    suspend fun receiveFollowJob(props: JobProps<ReceiveFollowJob>)
}
