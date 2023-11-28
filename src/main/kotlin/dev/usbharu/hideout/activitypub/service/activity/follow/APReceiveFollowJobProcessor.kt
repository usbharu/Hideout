package dev.usbharu.hideout.activitypub.service.activity.follow

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import dev.usbharu.hideout.activitypub.domain.model.Accept
import dev.usbharu.hideout.activitypub.domain.model.Follow
import dev.usbharu.hideout.activitypub.service.common.APRequestService
import dev.usbharu.hideout.activitypub.service.objects.user.APUserService
import dev.usbharu.hideout.application.external.Transaction
import dev.usbharu.hideout.core.external.job.ReceiveFollowJob
import dev.usbharu.hideout.core.external.job.ReceiveFollowJobParam
import dev.usbharu.hideout.core.query.UserQueryService
import dev.usbharu.hideout.core.service.job.JobProcessor
import dev.usbharu.hideout.core.service.user.UserService
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service

@Service
class APReceiveFollowJobProcessor(
    private val transaction: Transaction,
    private val userQueryService: UserQueryService,
    private val apUserService: APUserService,
    private val objectMapper: ObjectMapper,
    private val apRequestService: APRequestService,
    private val userService: UserService
) :
    JobProcessor<ReceiveFollowJobParam, ReceiveFollowJob> {
    override suspend fun process(param: ReceiveFollowJobParam) = transaction.transaction {
        val person = apUserService.fetchPerson(param.actor, param.targetActor)
        val follow = objectMapper.readValue<Follow>(param.follow)

        logger.info("START Follow from: {} to {}", param.targetActor, param.actor)

        val signer = userQueryService.findByUrl(param.targetActor)

        val urlString = person.inbox

        apRequestService.apPost(
            url = urlString,
            body = Accept(
                name = "Follow",
                `object` = follow,
                actor = param.targetActor
            ),
            signer = signer
        )

        val targetEntity = userQueryService.findByUrl(param.targetActor)
        val followActorEntity =
            userQueryService.findByUrl(follow.actor)

        userService.followRequest(targetEntity.id, followActorEntity.id)
        logger.info("SUCCESS Follow from: {} to: {}", param.targetActor, param.actor)
    }

    override fun job(): ReceiveFollowJob = ReceiveFollowJob

    companion object {
        private val logger = LoggerFactory.getLogger(APReceiveFollowJobProcessor::class.java)
    }
}
