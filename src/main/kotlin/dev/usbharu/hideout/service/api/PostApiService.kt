package dev.usbharu.hideout.service.api

import dev.usbharu.hideout.config.Config
import dev.usbharu.hideout.domain.model.hideout.dto.PostCreateDto
import dev.usbharu.hideout.domain.model.hideout.dto.PostResponse
import dev.usbharu.hideout.domain.model.hideout.dto.ReactionResponse
import dev.usbharu.hideout.domain.model.hideout.form.Post
import dev.usbharu.hideout.query.PostResponseQueryService
import dev.usbharu.hideout.query.ReactionQueryService
import dev.usbharu.hideout.repository.UserRepository
import dev.usbharu.hideout.service.core.Transaction
import dev.usbharu.hideout.service.post.PostService
import dev.usbharu.hideout.service.reaction.ReactionService
import dev.usbharu.hideout.util.AcctUtil
import org.koin.core.annotation.Single
import org.springframework.stereotype.Service
import java.time.Instant

@Suppress("LongParameterList")
@Service
interface PostApiService {
    suspend fun createPost(postForm: dev.usbharu.hideout.domain.model.hideout.form.Post, userId: Long): PostResponse
    suspend fun getById(id: Long, userId: Long?): PostResponse
    suspend fun getAll(
        since: Instant? = null,
        until: Instant? = null,
        minId: Long? = null,
        maxId: Long? = null,
        limit: Int? = null,
        userId: Long? = null
    ): List<PostResponse>

    suspend fun getByUser(
        nameOrId: String,
        since: Instant? = null,
        until: Instant? = null,
        minId: Long? = null,
        maxId: Long? = null,
        limit: Int? = null,
        userId: Long? = null
    ): List<PostResponse>

    suspend fun getReactionByPostId(postId: Long, userId: Long? = null): List<ReactionResponse>
    suspend fun appendReaction(reaction: String, userId: Long, postId: Long)
    suspend fun removeReaction(userId: Long, postId: Long)
}

@Single
@Service
class PostApiServiceImpl(
    private val postService: PostService,
    private val userRepository: UserRepository,
    private val postResponseQueryService: PostResponseQueryService,
    private val reactionQueryService: ReactionQueryService,
    private val reactionService: ReactionService,
    private val transaction: Transaction
) : PostApiService {
    override suspend fun createPost(postForm: Post, userId: Long): PostResponse {
        return transaction.transaction {
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
    ): List<PostResponse> = transaction.transaction {
        postResponseQueryService.findAll(
            since = since?.toEpochMilli(),
            until = until?.toEpochMilli(),
            minId = minId,
            maxId = maxId,
            limit = limit,
            userId = userId
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
        transaction.transaction { reactionQueryService.findByPostIdWithUsers(postId, userId) }

    override suspend fun appendReaction(reaction: String, userId: Long, postId: Long) {
        transaction.transaction {
            reactionService.sendReaction(reaction, userId, postId)
        }
    }

    override suspend fun removeReaction(userId: Long, postId: Long) {
        transaction.transaction {
            reactionService.removeReaction(userId, postId)
        }
    }
}
