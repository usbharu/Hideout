package dev.usbharu.hideout.activitypub.service.activity.like

import dev.usbharu.hideout.activitypub.domain.exception.FailedToGetActivityPubResourceException
import dev.usbharu.hideout.activitypub.domain.exception.IllegalActivityPubObjectException
import dev.usbharu.hideout.activitypub.domain.model.Like
import dev.usbharu.hideout.activitypub.service.common.AbstractActivityPubProcessor
import dev.usbharu.hideout.activitypub.service.common.ActivityPubProcessContext
import dev.usbharu.hideout.activitypub.service.common.ActivityType
import dev.usbharu.hideout.activitypub.service.objects.note.APNoteService
import dev.usbharu.hideout.activitypub.service.objects.user.APUserService
import dev.usbharu.hideout.application.external.Transaction
import dev.usbharu.hideout.core.query.PostQueryService
import dev.usbharu.hideout.core.service.reaction.ReactionService

class APLikeProcessor(
    transaction: Transaction,
    private val apUserService: APUserService,
    private val apNoteService: APNoteService,
    private val postQueryService: PostQueryService,
    private val reactionService: ReactionService
) :
    AbstractActivityPubProcessor<Like>(transaction) {
    override suspend fun internalProcess(activity: ActivityPubProcessContext<Like>) {
        val actor = activity.activity.actor ?: throw IllegalActivityPubObjectException("actor is null")
        val content = activity.activity.content ?: throw IllegalActivityPubObjectException("content is null")

        val target = activity.activity.`object` ?: throw IllegalActivityPubObjectException("object is null")

        val personWithEntity = apUserService.fetchPersonWithEntity(actor)

        try {
            apNoteService.fetchNoteAsync(target).await()
        } catch (e: FailedToGetActivityPubResourceException) {
            logger.debug("FAILED failed to get {}", target)
            logger.trace("", e)
            return
        }

        val post = postQueryService.findByUrl(target)

        reactionService.receiveReaction(
            content,
            actor.substringAfter("://").substringBefore("/"),
            personWithEntity.second.id,
            post.id
        )

        logger.debug("SUCCESS Add Like($content) from ${personWithEntity.second.url} to ${post.url}")
    }

    override fun isSupported(activityType: ActivityType): Boolean = activityType == ActivityType.Like

    override fun type(): Class<Like> = Like::class.java
}