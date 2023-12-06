package dev.usbharu.hideout.mastodon.service.status

import dev.usbharu.hideout.application.external.Transaction
import dev.usbharu.hideout.core.domain.exception.FailedToGetResourcesException
import dev.usbharu.hideout.core.domain.model.media.MediaRepository
import dev.usbharu.hideout.core.domain.model.media.toMediaAttachments
import dev.usbharu.hideout.core.query.PostQueryService
import dev.usbharu.hideout.core.query.UserQueryService
import dev.usbharu.hideout.core.service.post.PostCreateDto
import dev.usbharu.hideout.core.service.post.PostService
import dev.usbharu.hideout.domain.mastodon.model.generated.Status
import dev.usbharu.hideout.mastodon.interfaces.api.status.StatusesRequest
import dev.usbharu.hideout.mastodon.interfaces.api.status.toPostVisibility
import dev.usbharu.hideout.mastodon.interfaces.api.status.toStatusVisibility
import dev.usbharu.hideout.mastodon.service.account.AccountService
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.time.Instant

@Service
interface StatusesApiService {
    suspend fun postStatus(
        statusesRequest: StatusesRequest,
        userId: Long
    ): Status
}

@Service
class StatsesApiServiceImpl(
    private val postService: PostService,
    private val accountService: AccountService,
    private val postQueryService: PostQueryService,
    private val userQueryService: UserQueryService,
    private val mediaRepository: MediaRepository,
    private val transaction: Transaction
) :
    StatusesApiService {
    override suspend fun postStatus(
        statusesRequest: StatusesRequest,
        userId: Long
    ): Status = transaction.transaction {
        logger.debug("START create post by mastodon api. {}", statusesRequest)

        val post = postService.createLocal(
            PostCreateDto(
                text = statusesRequest.status.orEmpty(),
                overview = statusesRequest.spoiler_text,
                visibility = statusesRequest.visibility.toPostVisibility(),
                repolyId = statusesRequest.in_reply_to_id?.toLong(),
                userId = userId,
                mediaIds = statusesRequest.media_ids.map { it.toLong() }
            )
        )
        val account = accountService.findById(userId)

        val replyUser = if (post.replyId != null) {
            try {
                userQueryService.findById(postQueryService.findById(post.replyId).userId).id
            } catch (ignore: FailedToGetResourcesException) {
                null
            }
        } else {
            null
        }

        // TODO: n+1解消
        val mediaAttachment = post.mediaIds.map { mediaId ->
            mediaRepository.findById(mediaId)
        }.map {
            it.toMediaAttachments()
        }

        Status(
            id = post.id.toString(),
            uri = post.apId,
            createdAt = Instant.ofEpochMilli(post.createdAt).toString(),
            account = account,
            content = post.text,
            visibility = statusesRequest.visibility.toStatusVisibility(),
            sensitive = post.sensitive,
            spoilerText = post.overview.orEmpty(),
            mediaAttachments = mediaAttachment,
            mentions = emptyList(),
            tags = emptyList(),
            emojis = emptyList(),
            reblogsCount = 0,
            favouritesCount = 0,
            repliesCount = 0,
            url = post.url,
            inReplyToId = post.replyId?.toString(),
            inReplyToAccountId = replyUser?.toString(),
            language = null,
            text = post.text,
            editedAt = null,
        )
    }

    companion object {
        private val logger = LoggerFactory.getLogger(StatusesApiService::class.java)
    }
}
