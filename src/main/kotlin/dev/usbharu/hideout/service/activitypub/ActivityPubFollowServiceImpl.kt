package dev.usbharu.hideout.service.activitypub

import com.fasterxml.jackson.module.kotlin.readValue
import dev.usbharu.hideout.ap.Follow
import dev.usbharu.hideout.config.Config
import dev.usbharu.hideout.domain.model.ActivityPubResponse
import dev.usbharu.hideout.domain.model.ActivityPubStringResponse
import dev.usbharu.hideout.domain.model.job.ReceiveFollowJob
import dev.usbharu.hideout.plugins.postAp
import dev.usbharu.hideout.service.job.JobQueueParentService
import io.ktor.client.*
import io.ktor.http.*
import kjob.core.job.JobProps

class ActivityPubFollowServiceImpl(
    private val jobQueueParentService: JobQueueParentService,
    private val activityPubUserService: ActivityPubUserService,
    private val httpClient: HttpClient
) : ActivityPubFollowService {
    override suspend fun receiveFollow(follow: Follow): ActivityPubResponse {
        // TODO: Verify HTTP  Signature
        jobQueueParentService.schedule(ReceiveFollowJob) {
            props[it.actor] = follow.actor
            props[it.follow] = Config.configData.objectMapper.writeValueAsString(follow)
            props[it.targetActor] = follow.`object`
        }
        return ActivityPubStringResponse(HttpStatusCode.OK, "{}", ContentType.Application.Json)
    }

    override suspend fun receiveFollowJob(props: JobProps<ReceiveFollowJob>) {
        val actor = props[ReceiveFollowJob.actor]
        val person = activityPubUserService.fetchPerson(actor)
        val follow = Config.configData.objectMapper.readValue<Follow>(props[ReceiveFollowJob.follow])
        httpClient.postAp(
            urlString = person.inbox ?: throw IllegalArgumentException("inbox is not found"),
            username = "${props[ReceiveFollowJob.targetActor]}#pubkey",
            jsonLd = follow
        )
    }
}
