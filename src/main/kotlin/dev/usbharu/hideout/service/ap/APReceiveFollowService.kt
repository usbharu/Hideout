package dev.usbharu.hideout.service.ap

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import dev.usbharu.hideout.domain.model.ActivityPubResponse
import dev.usbharu.hideout.domain.model.ActivityPubStringResponse
import dev.usbharu.hideout.domain.model.ap.Accept
import dev.usbharu.hideout.domain.model.ap.Follow
import dev.usbharu.hideout.domain.model.job.ReceiveFollowJob
import dev.usbharu.hideout.query.UserQueryService
import dev.usbharu.hideout.service.core.Transaction
import dev.usbharu.hideout.service.job.JobQueueParentService
import dev.usbharu.hideout.service.user.UserService
import io.ktor.http.*
import kjob.core.job.JobProps
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.stereotype.Service

interface APReceiveFollowService {
    suspend fun receiveFollow(follow: Follow): ActivityPubResponse
    suspend fun receiveFollowJob(props: JobProps<ReceiveFollowJob>)
}

@Service
class APReceiveFollowServiceImpl(
    private val jobQueueParentService: JobQueueParentService,
    private val apUserService: APUserService,
    private val userService: UserService,
    private val userQueryService: UserQueryService,
    private val transaction: Transaction,
    @Qualifier("activitypub") private val objectMapper: ObjectMapper,
    private val apRequestService: APRequestService
) : APReceiveFollowService {
    override suspend fun receiveFollow(follow: Follow): ActivityPubResponse {
        // TODO: Verify HTTP  Signature
        jobQueueParentService.schedule(ReceiveFollowJob) {
            props[ReceiveFollowJob.actor] = follow.actor
            props[ReceiveFollowJob.follow] = objectMapper.writeValueAsString(follow)
            props[ReceiveFollowJob.targetActor] = follow.`object`
        }
        return ActivityPubStringResponse(HttpStatusCode.OK, "{}", ContentType.Application.Json)
    }

    override suspend fun receiveFollowJob(props: JobProps<ReceiveFollowJob>) {
//        throw Exception()
        transaction.transaction {
            val actor = props[ReceiveFollowJob.actor]
            val targetActor = props[ReceiveFollowJob.targetActor]
            val person = apUserService.fetchPerson(actor, targetActor)
            val follow = objectMapper.readValue<Follow>(props[ReceiveFollowJob.follow])

            val signer = userQueryService.findByUrl(actor)

            val urlString = person.inbox ?: throw IllegalArgumentException("inbox is not found")

            apRequestService.apPost(
                urlString,
                Accept(
                    name = "Follow",
                    `object` = follow,
                    actor = targetActor
                ),
                signer
            )

            val targetEntity = userQueryService.findByUrl(targetActor)
            val followActorEntity =
                userQueryService.findByUrl(follow.actor ?: throw java.lang.IllegalArgumentException("Actor is null"))

            userService.followRequest(targetEntity.id, followActorEntity.id)
        }
    }
}
