package dev.usbharu.hideout.service.api

import dev.usbharu.hideout.config.Config
import dev.usbharu.hideout.domain.model.hideout.dto.PostCreateDto
import dev.usbharu.hideout.domain.model.hideout.dto.PostResponse
import dev.usbharu.hideout.repository.*
import dev.usbharu.hideout.service.post.IPostService
import dev.usbharu.hideout.util.AcctUtil
import kotlinx.coroutines.Dispatchers
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.innerJoin
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import org.koin.core.annotation.Single
import java.time.Instant
import dev.usbharu.hideout.domain.model.hideout.form.Post as FormPost

@Single
class PostApiServiceImpl(
    private val postService: IPostService,
    private val postRepository: IPostRepository,
    private val userRepository: IUserRepository
) : IPostApiService {
    override suspend fun createPost(postForm: FormPost, userId: Long): PostResponse {
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
        return PostResponse.from(createdPost, creator!!)
    }

    @Suppress("InjectDispatcher")
    suspend fun <T> query(block: suspend () -> T): T =
        newSuspendedTransaction(Dispatchers.IO) { block() }

    override suspend fun getById(id: Long, userId: Long?): PostResponse {
        val query = query {
            Posts.innerJoin(Users, onColumn = { Posts.userId }, otherColumn = { Users.id }).select { Posts.id eq id }
                .single()
        }
        return PostResponse.from(query.toPost(), query.toUser())
    }

    override suspend fun getAll(
        since: Instant?,
        until: Instant?,
        minId: Long?,
        maxId: Long?,
        limit: Int?,
        userId: Long?
    ): List<PostResponse> {
        return query {
            Posts.innerJoin(Users, onColumn = { Posts.userId }, otherColumn = { id }).selectAll()
                .map { PostResponse.from(it.toPost(), it.toUser()) }
        }
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
            query {
                Posts.innerJoin(Users, onColumn = { Posts.userId }, otherColumn = { id }).select {
                    Users.name.eq(acct.username)
                        .and(Users.domain eq (acct.domain ?: Config.configData.domain))
                }.map { PostResponse.from(it.toPost(), it.toUser()) }
            }
        } else {
            query {
                Posts.innerJoin(Users, onColumn = { Posts.userId }, otherColumn = { id }).select {
                    Posts.userId eq idOrNull
                }.map { PostResponse.from(it.toPost(), it.toUser()) }
            }
        }
    }
}
