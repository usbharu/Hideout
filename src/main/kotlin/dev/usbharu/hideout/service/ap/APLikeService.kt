package dev.usbharu.hideout.service.ap

import dev.usbharu.hideout.domain.model.ActivityPubResponse
import dev.usbharu.hideout.domain.model.ActivityPubStringResponse
import dev.usbharu.hideout.domain.model.ap.Like
import dev.usbharu.hideout.exception.ap.IllegalActivityPubObjectException
import dev.usbharu.hideout.query.PostQueryService
import dev.usbharu.hideout.service.core.Transaction
import dev.usbharu.hideout.service.reaction.ReactionService
import io.ktor.http.*
import org.koin.core.annotation.Single
import org.springframework.stereotype.Service

@Service
interface APLikeService {
    suspend fun receiveLike(like: Like): ActivityPubResponse
}

@Single
@Service
class APLikeServiceImpl(
    private val reactionService: ReactionService,
    private val apUserService: APUserService,
    private val apNoteService: APNoteService,
    private val postQueryService: PostQueryService,
    private val transaction: Transaction
) : APLikeService {
    override suspend fun receiveLike(like: Like): ActivityPubResponse {
        val actor = like.actor ?: throw IllegalActivityPubObjectException("actor is null")
        val content = like.content ?: throw IllegalActivityPubObjectException("content is null")
        like.`object` ?: throw IllegalActivityPubObjectException("object is null")
        transaction.transaction(java.sql.Connection.TRANSACTION_SERIALIZABLE) {
            val person = apUserService.fetchPersonWithEntity(actor)
            apNoteService.fetchNote(like.`object`!!)

            val post = postQueryService.findByUrl(like.`object`!!)

            reactionService.receiveReaction(
                content,
                actor.substringAfter("://").substringBefore("/"),
                person.second.id,
                post.id
            )
        }
        return ActivityPubStringResponse(HttpStatusCode.OK, "")
    }
}
