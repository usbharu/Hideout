package dev.usbharu.hideout.activitypub.service.activity.follow

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import dev.usbharu.hideout.activitypub.domain.model.Accept
import dev.usbharu.hideout.activitypub.domain.model.Follow
import dev.usbharu.hideout.activitypub.service.common.APRequestService
import dev.usbharu.hideout.activitypub.service.objects.user.APUserService
import dev.usbharu.hideout.application.external.Transaction
import dev.usbharu.hideout.core.external.job.ReceiveFollowJob
import dev.usbharu.hideout.core.query.UserQueryService
import dev.usbharu.hideout.core.service.user.UserService
import kjob.core.job.JobProps
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.stereotype.Component

@Component
class APReceiveFollowJobServiceImpl(
    private val apUserService: APUserService,
    private val userQueryService: UserQueryService,
    private val apRequestService: APRequestService,
    private val userService: UserService,
    @Qualifier("activitypub") private val objectMapper: ObjectMapper,
    private val transaction: Transaction
) : APReceiveFollowJobService {
    override suspend fun receiveFollowJob(props: JobProps<ReceiveFollowJob>) {
        transaction.transaction {
            val actor = props[ReceiveFollowJob.actor]
            val targetActor = props[ReceiveFollowJob.targetActor]
            val person = apUserService.fetchPerson(actor, targetActor)
            val follow = objectMapper.readValue<Follow>(props[ReceiveFollowJob.follow])
            logger.info("START Follow from: {} to: {}", targetActor, actor)

            val signer = userQueryService.findByUrl(targetActor)

            val urlString = person.inbox ?: throw IllegalArgumentException("inbox is not found")

            apRequestService.apPost(
                url = urlString,
                body = Accept(
                    name = "Follow",
                    `object` = follow,
                    actor = targetActor
                ),
                signer = signer
            )

            val targetEntity = userQueryService.findByUrl(targetActor)
            val followActorEntity =
                userQueryService.findByUrl(follow.actor ?: throw java.lang.IllegalArgumentException("Actor is null"))

            userService.followRequest(targetEntity.id, followActorEntity.id)
            logger.info("SUCCESS Follow from: {} to: {}", targetActor, actor)
        }
    }

    companion object {
        private val logger = LoggerFactory.getLogger(APReceiveFollowJobServiceImpl::class.java)
    }
}
