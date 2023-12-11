package dev.usbharu.hideout.activitypub.service.activity.reject

import dev.usbharu.hideout.activitypub.domain.model.Follow
import dev.usbharu.hideout.activitypub.domain.model.Reject
import dev.usbharu.hideout.activitypub.service.common.AbstractActivityPubProcessor
import dev.usbharu.hideout.activitypub.service.common.ActivityPubProcessContext
import dev.usbharu.hideout.activitypub.service.common.ActivityType
import dev.usbharu.hideout.application.external.Transaction
import dev.usbharu.hideout.core.query.UserQueryService
import dev.usbharu.hideout.core.service.relationship.RelationshipService
import org.springframework.stereotype.Service


@Service
class ApRejectProcessor(
    private val relationshipService: RelationshipService,
    private val userQueryService: UserQueryService,
    transaction: Transaction
) :
    AbstractActivityPubProcessor<Reject>(transaction) {
    override suspend fun internalProcess(activity: ActivityPubProcessContext<Reject>) {

        val activityType = activity.activity.apObject.type.firstOrNull { it == "Follow" }

        if (activityType == null) {
            logger.warn("FAILED Process Reject Activity type: {}", activity.activity.apObject.type)
            return
        }
        when (activityType) {
            "Follow" -> {
                val user = userQueryService.findByUrl(activity.activity.actor)

                activity.activity.apObject as Follow

                val actor = activity.activity.apObject.actor

                val target = userQueryService.findByUrl(actor)

                logger.debug("REJECT Follow user {} target {}", user.url, target.url)

                relationshipService.rejectFollowRequest(user.id, target.id)
            }

            else -> {}
        }
    }

    override fun isSupported(activityType: ActivityType): Boolean = activityType == ActivityType.Reject

    override fun type(): Class<Reject> = Reject::class.java
}
