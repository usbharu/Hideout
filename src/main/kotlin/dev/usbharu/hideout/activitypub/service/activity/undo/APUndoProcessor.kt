package dev.usbharu.hideout.activitypub.service.activity.undo

import dev.usbharu.hideout.activitypub.domain.model.Accept
import dev.usbharu.hideout.activitypub.domain.model.Block
import dev.usbharu.hideout.activitypub.domain.model.Follow
import dev.usbharu.hideout.activitypub.domain.model.Undo
import dev.usbharu.hideout.activitypub.domain.model.objects.ObjectValue
import dev.usbharu.hideout.activitypub.service.common.AbstractActivityPubProcessor
import dev.usbharu.hideout.activitypub.service.common.ActivityPubProcessContext
import dev.usbharu.hideout.activitypub.service.common.ActivityType
import dev.usbharu.hideout.activitypub.service.objects.user.APUserService
import dev.usbharu.hideout.application.external.Transaction
import dev.usbharu.hideout.core.query.ActorQueryService
import dev.usbharu.hideout.core.service.relationship.RelationshipService
import org.springframework.stereotype.Service

@Service
class APUndoProcessor(
    transaction: Transaction,
    private val apUserService: APUserService,
    private val actorQueryService: ActorQueryService,
    private val relationshipService: RelationshipService
) :
    AbstractActivityPubProcessor<Undo>(transaction) {
    override suspend fun internalProcess(activity: ActivityPubProcessContext<Undo>) {
        val undo = activity.activity

        val type =
            undo.apObject.type
                .firstOrNull { it == "Block" || it == "Follow" || it == "Like" || it == "Announce" || it == "Accept" }
                ?: return

        when (type) {
            "Follow" -> {
                val follow = undo.apObject as Follow

                apUserService.fetchPerson(undo.actor, follow.apObject)
                val follower = actorQueryService.findByUrl(undo.actor)
                val target = actorQueryService.findByUrl(follow.apObject)

                relationshipService.unfollow(follower.id, target.id)
                return
            }

            "Block" -> {
                val block = undo.apObject as Block

                val blocker = apUserService.fetchPersonWithEntity(undo.actor, block.apObject).second
                val target = actorQueryService.findByUrl(block.apObject)

                relationshipService.unblock(blocker.id, target.id)
                return
            }

            "Accept" -> {
                val accept = undo.apObject as Accept

                val acceptObject = if (accept.apObject is ObjectValue) {
                    accept.apObject.`object`
                } else if (accept.apObject is Follow) {
                    accept.apObject.apObject
                } else {
                    logger.warn("FAILED Unsupported type. Undo Accept {}", accept.apObject.type)
                    return
                }

                val accepter = apUserService.fetchPersonWithEntity(undo.actor, acceptObject).second
                val target = actorQueryService.findByUrl(acceptObject)

                relationshipService.rejectFollowRequest(accepter.id, target.id)
            }

            else -> {}
        }
        TODO()
    }

    override fun isSupported(activityType: ActivityType): Boolean = activityType == ActivityType.Undo

    override fun type(): Class<Undo> = Undo::class.java
}
