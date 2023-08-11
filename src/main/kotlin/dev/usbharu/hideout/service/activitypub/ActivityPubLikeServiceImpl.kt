package dev.usbharu.hideout.service.activitypub

import dev.usbharu.hideout.domain.model.ActivityPubResponse
import dev.usbharu.hideout.domain.model.ActivityPubStringResponse
import dev.usbharu.hideout.domain.model.ap.Like
import dev.usbharu.hideout.exception.ap.IllegalActivityPubObjectException
import dev.usbharu.hideout.query.PostQueryService
import dev.usbharu.hideout.query.UserQueryService
import dev.usbharu.hideout.service.core.Transaction
import dev.usbharu.hideout.service.reaction.IReactionService
import io.ktor.http.*
import org.koin.core.annotation.Single

@Single
class ActivityPubLikeServiceImpl(
    private val reactionService: IReactionService,
    private val activityPubUserService: ActivityPubUserService,
    private val activityPubNoteService: ActivityPubNoteService,
    private val userQueryService: UserQueryService,
    private val postQueryService: PostQueryService,
    private val transaction: Transaction
) : ActivityPubLikeService {
    override suspend fun receiveLike(like: Like): ActivityPubResponse {
        val actor = like.actor ?: throw IllegalActivityPubObjectException("actor is null")
        val content = like.content ?: throw IllegalActivityPubObjectException("content is null")
        like.`object` ?: throw IllegalActivityPubObjectException("object is null")
        transaction.transaction {
            val person = activityPubUserService.fetchPerson(actor)
            activityPubNoteService.fetchNote(like.`object`!!)

            val user = userQueryService.findByUrl(
                person.url
                    ?: throw IllegalActivityPubObjectException("actor is not found")
            )

            val post = postQueryService.findByUrl(like.`object`!!)

            reactionService.receiveReaction(content, actor.substringAfter("://").substringBefore("/"), user.id, post.id)
        }
        return ActivityPubStringResponse(HttpStatusCode.OK, "")
    }
}
