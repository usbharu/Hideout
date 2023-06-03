package dev.usbharu.hideout.service.api

import dev.usbharu.hideout.config.Config
import dev.usbharu.hideout.domain.model.hideout.dto.PostCreateDto
import dev.usbharu.hideout.domain.model.hideout.entity.Post
import dev.usbharu.hideout.exception.PostNotFoundException
import dev.usbharu.hideout.repository.IPostRepository
import dev.usbharu.hideout.service.post.IPostService
import dev.usbharu.hideout.util.AcctUtil
import org.koin.core.annotation.Single
import java.time.Instant
import dev.usbharu.hideout.domain.model.hideout.form.Post as FormPost

@Single
class PostApiServiceImpl(
        private val postService: IPostService,
        private val postRepository: IPostRepository
) : IPostApiService {
    override suspend fun createPost(postForm: FormPost, userId: Long): Post {
        return postService.createLocal(
                PostCreateDto(
                        text = postForm.text,
                        overview = postForm.overview,
                        visibility = postForm.visibility,
                        repostId = postForm.repostId,
                        repolyId = postForm.replyId,
                        userId = userId
                )
        )
    }

    override suspend fun getById(id: Long, userId: Long?): Post {
        return postRepository.findOneById(id, userId)
                ?: throw PostNotFoundException("$id was not found or is not authorized.")
    }

    override suspend fun getAll(
            since: Instant?,
            until: Instant?,
            minId: Long?,
            maxId: Long?,
            limit: Int?,
            userId: Long?
    ): List<Post> = postRepository.findAll(since, until, minId, maxId, limit, userId)

    override suspend fun getByUser(
            nameOrId: String,
            since: Instant?,
            until: Instant?,
            minId: Long?,
            maxId: Long?,
            limit: Int?,
            userId: Long?
    ): List<Post> {
        val idOrNull = nameOrId.toLongOrNull()
        return if (idOrNull == null) {
            val acct = AcctUtil.parse(nameOrId)
            postRepository.findByUserNameAndDomain(
                    acct.username,
                    acct.domain
                            ?: Config.configData.domain,
                    since,
                    until,
                    minId,
                    maxId,
                    limit,
                    userId
            )
        } else {
            postRepository.findByUserId(idOrNull, since, until, minId, maxId, limit, userId)
        }
    }
}
