package dev.usbharu.hideout.activitypub.service.activity.accept

import dev.usbharu.hideout.activitypub.domain.exception.IllegalActivityPubObjectException
import dev.usbharu.hideout.activitypub.domain.model.Accept
import dev.usbharu.hideout.activitypub.domain.model.Follow
import dev.usbharu.hideout.activitypub.service.common.AbstractActivityPubProcessor
import dev.usbharu.hideout.activitypub.service.common.ActivityPubProcessContext
import dev.usbharu.hideout.activitypub.service.common.ActivityType
import dev.usbharu.hideout.application.external.Transaction
import dev.usbharu.hideout.core.domain.exception.resource.UserNotFoundException
import dev.usbharu.hideout.core.domain.model.actor.ActorRepository
import dev.usbharu.hideout.core.service.relationship.RelationshipService
import org.springframework.stereotype.Service

@Service
class ApAcceptProcessor(
    transaction: Transaction,
    private val relationshipService: RelationshipService,
    private val actorRepository: ActorRepository
) :
    AbstractActivityPubProcessor<Accept>(transaction) {

    override suspend fun internalProcess(activity: ActivityPubProcessContext<Accept>) {
        val value = activity.activity.apObject

        if (value.type.contains("Follow").not()) {
            logger.warn("FAILED Activity type isn't Follow.")
            throw IllegalActivityPubObjectException("Invalid type ${value.type}")
        }

        val follow = value as Follow

        val userUrl = follow.apObject
        val followerUrl = follow.actor

        val user = actorRepository.findByUrl(userUrl) ?: throw UserNotFoundException.withUrl(userUrl)
        val follower = actorRepository.findByUrl(followerUrl) ?: throw UserNotFoundException.withUrl(followerUrl)

        relationshipService.acceptFollowRequest(user.id, follower.id)
        logger.debug("SUCCESS Follow from ${user.url} to ${follower.url}.")
    }

    override fun isSupported(activityType: ActivityType): Boolean = activityType == ActivityType.Accept

    override fun type(): Class<Accept> = Accept::class.java
}
