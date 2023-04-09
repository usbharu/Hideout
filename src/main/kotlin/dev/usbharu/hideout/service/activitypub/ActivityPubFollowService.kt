package dev.usbharu.hideout.service.activitypub

import dev.usbharu.hideout.ap.Follow
import dev.usbharu.hideout.domain.model.ActivityPubResponse
import dev.usbharu.hideout.domain.model.job.ReceiveFollowJob
import kjob.core.job.JobProps

interface ActivityPubFollowService {
    suspend fun receiveFollow(follow:Follow):ActivityPubResponse
    suspend fun receiveFollowJob(props: JobProps<ReceiveFollowJob>)
}
