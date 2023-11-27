package dev.usbharu.hideout.activitypub.service.activity.accept

import dev.usbharu.hideout.activitypub.domain.exception.IllegalActivityPubObjectException
import dev.usbharu.hideout.activitypub.domain.model.Accept
import dev.usbharu.hideout.activitypub.domain.model.Follow
import dev.usbharu.hideout.activitypub.service.common.AbstractActivityPubProcessor
import dev.usbharu.hideout.activitypub.service.common.ActivityPubProcessContext
import dev.usbharu.hideout.activitypub.service.common.ActivityType
import dev.usbharu.hideout.application.external.Transaction
import dev.usbharu.hideout.core.query.FollowerQueryService
import dev.usbharu.hideout.core.query.UserQueryService
import dev.usbharu.hideout.core.service.user.UserService

class ApAcceptProcessor(
    private val transaction: Transaction,
    private val userQueryService: UserQueryService,
    private val followerQueryService: FollowerQueryService,
    private val userService: UserService
) :
    AbstractActivityPubProcessor<Accept>(transaction) {

    override suspend fun internalProcess(activity: ActivityPubProcessContext<Accept>) {
        val value = activity.activity.`object` ?: throw IllegalActivityPubObjectException("object is null")

        if (value.type.contains("Follow").not()) {
            logger.warn("FAILED Activity type is not Follow.")
            throw IllegalActivityPubObjectException("Invalid type ${value.type}")
        }

        val follow = value as Follow

        val userUrl = follow.`object` ?: throw IllegalActivityPubObjectException("object is null")
        val followerUrl = follow.actor ?: throw IllegalActivityPubObjectException("actor is null")

        val user = userQueryService.findByUrl(userUrl)
        val follower = userQueryService.findByUrl(followerUrl)

        if (followerQueryService.alreadyFollow(user.id, follower.id)) {
            logger.debug("END User already follow from ${follower.url} to ${user.url}.")
            return
        }

        userService.follow(user.id, follower.id)
        logger.debug("SUCCESS Follow from ${follower.url} to ${user.url}.")
    }

    override fun isSupported(activityType: ActivityType): Boolean = activityType == ActivityType.Accept

    override fun type(): Class<Accept> = Accept::class.java
}
