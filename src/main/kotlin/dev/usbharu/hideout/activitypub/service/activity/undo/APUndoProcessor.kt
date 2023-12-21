package dev.usbharu.hideout.activitypub.service.activity.undo

import dev.usbharu.hideout.activitypub.domain.model.*
import dev.usbharu.hideout.activitypub.domain.model.objects.ObjectValue
import dev.usbharu.hideout.activitypub.service.common.AbstractActivityPubProcessor
import dev.usbharu.hideout.activitypub.service.common.ActivityPubProcessContext
import dev.usbharu.hideout.activitypub.service.common.ActivityType
import dev.usbharu.hideout.activitypub.service.objects.user.APUserService
import dev.usbharu.hideout.application.external.Transaction
import dev.usbharu.hideout.core.domain.exception.resource.PostNotFoundException
import dev.usbharu.hideout.core.domain.exception.resource.UserNotFoundException
import dev.usbharu.hideout.core.domain.exception.resource.local.LocalUserNotFoundException
import dev.usbharu.hideout.core.domain.model.actor.ActorRepository
import dev.usbharu.hideout.core.domain.model.post.PostRepository
import dev.usbharu.hideout.core.service.reaction.ReactionService
import dev.usbharu.hideout.core.service.relationship.RelationshipService
import org.springframework.stereotype.Service

@Service
class APUndoProcessor(
    transaction: Transaction,
    private val apUserService: APUserService,
    private val relationshipService: RelationshipService,
    private val reactionService: ReactionService,
    private val actorRepository: ActorRepository,
    private val postRepository: PostRepository
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

                val follower = apUserService.fetchPersonWithEntity(undo.actor, follow.apObject).second
                val target =
                    actorRepository.findByUrl(follow.apObject) ?: throw UserNotFoundException.withUrl(follow.apObject)

                relationshipService.unfollow(follower.id, target.id)
                return
            }

            "Block" -> {
                val block = undo.apObject as Block

                val blocker = apUserService.fetchPersonWithEntity(undo.actor, block.apObject).second
                val target =
                    actorRepository.findByUrl(block.apObject) ?: throw UserNotFoundException.withUrl(block.apObject)

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
                val target =
                    actorRepository.findByUrl(acceptObject) ?: throw UserNotFoundException.withUrl(acceptObject)

                relationshipService.rejectFollowRequest(accepter.id, target.id)
                return
            }

            "Like" -> {
                val like = undo.apObject as Like

                val post =
                    postRepository.findByUrl(like.apObject) ?: throw PostNotFoundException.withUrl(like.apObject)

                val signer =
                    actorRepository.findById(post.actorId) ?: throw LocalUserNotFoundException.withId(post.actorId)
                val actor = apUserService.fetchPersonWithEntity(like.actor, signer.url).second

                reactionService.receiveRemoveReaction(actor.id, post.id)
                return
            }

            else -> {}
        }
        TODO()
    }

    override fun isSupported(activityType: ActivityType): Boolean = activityType == ActivityType.Undo

    override fun type(): Class<Undo> = Undo::class.java
}
