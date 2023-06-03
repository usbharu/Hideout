package dev.usbharu.hideout.service.activitypub

import com.fasterxml.jackson.module.kotlin.readValue
import dev.usbharu.hideout.config.Config
import dev.usbharu.hideout.domain.model.ActivityPubResponse
import dev.usbharu.hideout.domain.model.ActivityPubStringResponse
import dev.usbharu.hideout.domain.model.ap.Accept
import dev.usbharu.hideout.domain.model.ap.Follow
import dev.usbharu.hideout.domain.model.job.ReceiveFollowJob
import dev.usbharu.hideout.plugins.postAp
import dev.usbharu.hideout.service.job.JobQueueParentService
import dev.usbharu.hideout.service.user.IUserService
import io.ktor.client.*
import io.ktor.http.*
import kjob.core.job.JobProps
import org.koin.core.annotation.Single

@Single
class ActivityPubReceiveFollowServiceImpl(
    private val jobQueueParentService: JobQueueParentService,
    private val activityPubUserService: ActivityPubUserService,
    private val userService: IUserService,
    private val httpClient: HttpClient
) : ActivityPubReceiveFollowService {
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
        val targetActor = props[ReceiveFollowJob.targetActor]
        val person = activityPubUserService.fetchPerson(actor, targetActor)
        val follow = Config.configData.objectMapper.readValue<Follow>(props[ReceiveFollowJob.follow])
        httpClient.postAp(
            urlString = person.inbox ?: throw IllegalArgumentException("inbox is not found"),
            username = "$targetActor#pubkey",
            jsonLd = Accept(
                name = "Follow",
                `object` = follow,
                actor = targetActor
            )
        )
        val users =
            userService.findByUrls(listOf(targetActor, follow.actor ?: throw IllegalArgumentException("actor is null")))

        userService.followRequest(users.first { it.url == targetActor }.id, users.first { it.url == follow.actor }.id)
    }
}
