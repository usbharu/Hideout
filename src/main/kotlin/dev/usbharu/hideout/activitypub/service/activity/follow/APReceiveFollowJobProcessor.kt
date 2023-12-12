package dev.usbharu.hideout.activitypub.service.activity.follow

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import dev.usbharu.hideout.activitypub.domain.model.Follow
import dev.usbharu.hideout.activitypub.service.objects.user.APUserService
import dev.usbharu.hideout.application.external.Transaction
import dev.usbharu.hideout.core.external.job.ReceiveFollowJob
import dev.usbharu.hideout.core.external.job.ReceiveFollowJobParam
import dev.usbharu.hideout.core.query.ActorQueryService
import dev.usbharu.hideout.core.service.job.JobProcessor
import dev.usbharu.hideout.core.service.relationship.RelationshipService
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service

@Service
class APReceiveFollowJobProcessor(
    private val transaction: Transaction,
    private val actorQueryService: ActorQueryService,
    private val apUserService: APUserService,
    private val objectMapper: ObjectMapper,
    private val relationshipService: RelationshipService
) :
    JobProcessor<ReceiveFollowJobParam, ReceiveFollowJob> {
    override suspend fun process(param: ReceiveFollowJobParam) = transaction.transaction {
        apUserService.fetchPerson(param.actor, param.targetActor)
        val follow = objectMapper.readValue<Follow>(param.follow)

        logger.info("START Follow from: {} to {}", param.targetActor, param.actor)

        val targetEntity = actorQueryService.findByUrl(param.targetActor)
        val followActorEntity =
            actorQueryService.findByUrl(follow.actor)

        relationshipService.followRequest(followActorEntity.id, targetEntity.id)

        logger.info("SUCCESS Follow from: {} to: {}", param.targetActor, param.actor)
    }

    override fun job(): ReceiveFollowJob = ReceiveFollowJob

    companion object {
        private val logger = LoggerFactory.getLogger(APReceiveFollowJobProcessor::class.java)
    }
}
