package dev.usbharu.hideout.service.activitypub

import dev.usbharu.hideout.domain.model.ActivityPubResponse
import dev.usbharu.hideout.domain.model.ActivityPubStringResponse
import dev.usbharu.hideout.domain.model.ap.Like
import dev.usbharu.hideout.exception.PostNotFoundException
import dev.usbharu.hideout.exception.ap.IllegalActivityPubObjectException
import dev.usbharu.hideout.repository.IPostRepository
import dev.usbharu.hideout.service.reaction.IReactionService
import dev.usbharu.hideout.service.user.IUserService
import io.ktor.http.*
import org.koin.core.annotation.Single


@Single
class ActivityPubLikeServiceImpl(private val reactionService: IReactionService,
                                 private val activityPubUserService: ActivityPubUserService,
                                 private val userService: IUserService,
                                 private val postService: IPostRepository,
                                 private val activityPubNoteService: ActivityPubNoteService) : ActivityPubLikeService {
    override suspend fun receiveLike(like: Like): ActivityPubResponse {
        val actor = like.actor ?: throw IllegalActivityPubObjectException("actor is null")
        val content = like.content ?: throw IllegalActivityPubObjectException("content is null")
        like.`object` ?: throw IllegalActivityPubObjectException("object is null")
        val person = activityPubUserService.fetchPerson(actor)
        activityPubNoteService.fetchNote(like.`object`!!)

        val user = userService.findByUrl(person.url
                ?: throw IllegalActivityPubObjectException("actor is not found"))

        val post = postService.findByUrl(like.`object`!!)
                ?: throw PostNotFoundException("${like.`object`} was not found")

        reactionService.receiveReaction(content, actor.substringAfter("://").substringBefore("/"), user.id, post.id)
        return ActivityPubStringResponse(HttpStatusCode.OK, "")
    }
}
