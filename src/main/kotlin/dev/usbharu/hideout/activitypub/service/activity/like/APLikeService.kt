package dev.usbharu.hideout.activitypub.service.activity.like

import dev.usbharu.hideout.activitypub.domain.exception.FailedToGetActivityPubResourceException
import dev.usbharu.hideout.activitypub.domain.exception.IllegalActivityPubObjectException
import dev.usbharu.hideout.activitypub.domain.model.Like
import dev.usbharu.hideout.activitypub.interfaces.api.common.ActivityPubResponse
import dev.usbharu.hideout.activitypub.interfaces.api.common.ActivityPubStringResponse
import dev.usbharu.hideout.activitypub.service.`object`.note.APNoteService
import dev.usbharu.hideout.activitypub.service.`object`.user.APUserService
import dev.usbharu.hideout.application.external.Transaction
import dev.usbharu.hideout.core.query.PostQueryService
import dev.usbharu.hideout.core.service.reaction.ReactionService
import io.ktor.http.*
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service

interface APLikeService {
    suspend fun receiveLike(like: Like): ActivityPubResponse
}

@Service
class APLikeServiceImpl(
    private val reactionService: ReactionService,
    private val apUserService: APUserService,
    private val apNoteService: APNoteService,
    private val postQueryService: PostQueryService,
    private val transaction: Transaction
) : APLikeService {
    override suspend fun receiveLike(like: Like): ActivityPubResponse {
        LOGGER.debug("START Add Like")
        LOGGER.trace("{}", like)

        val actor = like.actor ?: throw IllegalActivityPubObjectException("actor is null")
        val content = like.content ?: throw IllegalActivityPubObjectException("content is null")
        like.`object` ?: throw IllegalActivityPubObjectException("object is null")
        transaction.transaction {
            LOGGER.trace("FETCH Liked Person $actor")
            val person = apUserService.fetchPersonWithEntity(actor)
            LOGGER.trace("{}", person.second)

            LOGGER.trace("FETCH Liked Note ${like.`object`}")
            try {
                apNoteService.fetchNoteAsync(like.`object` ?: return@transaction).await()
            } catch (e: FailedToGetActivityPubResourceException) {
                LOGGER.debug("FAILED Failed to Get ${like.`object`}")
                LOGGER.trace("", e)
                return@transaction
            }
            val post = postQueryService.findByUrl(like.`object` ?: return@transaction)
            LOGGER.trace("{}", post)

            reactionService.receiveReaction(
                content,
                actor.substringAfter("://").substringBefore("/"),
                person.second.id,
                post.id
            )
            LOGGER.debug("SUCCESS Add Like($content) from ${person.second.url} to ${post.url}")
        }
        return ActivityPubStringResponse(HttpStatusCode.OK, "")
    }

    companion object {
        private val LOGGER = LoggerFactory.getLogger(APLikeServiceImpl::class.java)
    }
}
