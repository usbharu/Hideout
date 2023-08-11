package dev.usbharu.hideout.service.api

import dev.usbharu.hideout.config.Config
import dev.usbharu.hideout.domain.model.hideout.dto.PostCreateDto
import dev.usbharu.hideout.domain.model.hideout.dto.PostResponse
import dev.usbharu.hideout.domain.model.hideout.dto.ReactionResponse
import dev.usbharu.hideout.query.PostResponseQueryService
import dev.usbharu.hideout.query.ReactionQueryService
import dev.usbharu.hideout.repository.IUserRepository
import dev.usbharu.hideout.service.post.IPostService
import dev.usbharu.hideout.service.reaction.IReactionService
import dev.usbharu.hideout.util.AcctUtil
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import org.koin.core.annotation.Single
import java.time.Instant
import dev.usbharu.hideout.domain.model.hideout.form.Post as FormPost

@Single
class PostApiServiceImpl(
    private val postService: IPostService,
    private val userRepository: IUserRepository,
    private val postResponseQueryService: PostResponseQueryService,
    private val reactionQueryService: ReactionQueryService,
    private val reactionService: IReactionService
) : IPostApiService {
    override suspend fun createPost(postForm: FormPost, userId: Long): PostResponse {
        return newSuspendedTransaction {
            val createdPost = postService.createLocal(
                PostCreateDto(
                    text = postForm.text,
                    overview = postForm.overview,
                    visibility = postForm.visibility,
                    repostId = postForm.repostId,
                    repolyId = postForm.replyId,
                    userId = userId
                )
            )
            val creator = userRepository.findById(userId)
            PostResponse.from(createdPost, creator!!)
        }
    }

    override suspend fun getById(id: Long, userId: Long?): PostResponse = postResponseQueryService.findById(id, userId)

    override suspend fun getAll(
        since: Instant?,
        until: Instant?,
        minId: Long?,
        maxId: Long?,
        limit: Int?,
        userId: Long?
    ): List<PostResponse> = newSuspendedTransaction {
        postResponseQueryService.findAll(
            since?.toEpochMilli(),
            until?.toEpochMilli(),
            minId,
            maxId,
            limit,
            userId
        )
    }

    override suspend fun getByUser(
        nameOrId: String,
        since: Instant?,
        until: Instant?,
        minId: Long?,
        maxId: Long?,
        limit: Int?,
        userId: Long?
    ): List<PostResponse> {
        val idOrNull = nameOrId.toLongOrNull()
        return if (idOrNull == null) {
            val acct = AcctUtil.parse(nameOrId)
            postResponseQueryService.findByUserNameAndUserDomain(acct.username, acct.domain ?: Config.configData.domain)
        } else {
            postResponseQueryService.findByUserId(idOrNull)
        }
    }

    override suspend fun getReactionByPostId(postId: Long, userId: Long?): List<ReactionResponse> =
        newSuspendedTransaction { reactionQueryService.findByPostIdWithUsers(postId, userId) }

    override suspend fun appendReaction(reaction: String, userId: Long, postId: Long) {
        newSuspendedTransaction {
            reactionService.sendReaction(reaction, userId, postId)
        }
    }

    override suspend fun removeReaction(userId: Long, postId: Long) {
        newSuspendedTransaction {
            reactionService.removeReaction(userId, postId)
        }
    }
}
