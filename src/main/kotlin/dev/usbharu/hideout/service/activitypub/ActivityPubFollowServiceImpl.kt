package dev.usbharu.hideout.service.activitypub

import dev.usbharu.hideout.ap.Follow
import dev.usbharu.hideout.domain.model.ActivityPubResponse
import dev.usbharu.hideout.domain.model.ActivityPubStringResponse
import dev.usbharu.hideout.domain.model.job.AcceptFollowJob
import dev.usbharu.hideout.service.job.JobQueueService
import io.ktor.http.*

class ActivityPubFollowServiceImpl(private val jobQueueService: JobQueueService) : ActivityPubFollowService {
    override suspend fun receiveFollow(follow: Follow): ActivityPubResponse {
        // TODO: Verify HTTP  Signature
        jobQueueService.schedule(AcceptFollowJob)
        return ActivityPubStringResponse(HttpStatusCode.OK,"{}",ContentType.Application.Json)
    }
}
