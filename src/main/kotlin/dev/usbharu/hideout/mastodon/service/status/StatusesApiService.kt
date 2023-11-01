package dev.usbharu.hideout.mastodon.service.status

import dev.usbharu.hideout.application.external.Transaction
import dev.usbharu.hideout.core.domain.exception.FailedToGetResourcesException
import dev.usbharu.hideout.core.domain.model.media.MediaRepository
import dev.usbharu.hideout.core.domain.model.post.Visibility
import dev.usbharu.hideout.core.query.PostQueryService
import dev.usbharu.hideout.core.query.UserQueryService
import dev.usbharu.hideout.core.service.media.FileType
import dev.usbharu.hideout.core.service.post.PostCreateDto
import dev.usbharu.hideout.core.service.post.PostService
import dev.usbharu.hideout.domain.mastodon.model.generated.MediaAttachment
import dev.usbharu.hideout.domain.mastodon.model.generated.Status
import dev.usbharu.hideout.mastodon.interfaces.api.status.StatusesRequest
import dev.usbharu.hideout.mastodon.service.account.AccountService
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
    @Suppress("LongMethod", "CyclomaticComplexMethod")
    override suspend fun postStatus(
        statusesRequest: StatusesRequest,
        userId: Long
    ): Status = transaction.transaction {
        val visibility = when (statusesRequest.visibility) {
            StatusesRequest.Visibility.public -> Visibility.PUBLIC
            StatusesRequest.Visibility.unlisted -> Visibility.UNLISTED
            StatusesRequest.Visibility.private -> Visibility.FOLLOWERS
            StatusesRequest.Visibility.direct -> Visibility.DIRECT
            null -> Visibility.PUBLIC
        }

        val post = postService.createLocal(
            PostCreateDto(
                text = statusesRequest.status.orEmpty(),
                overview = statusesRequest.spoiler_text,
                visibility = visibility,
                repolyId = statusesRequest.in_reply_to_id?.toLongOrNull(),
                userId = userId,
                mediaIds = statusesRequest.media_ids.map { it.toLong() }
            )
        )
        val account = accountService.findById(userId)

        val postVisibility = when (statusesRequest.visibility) {
            StatusesRequest.Visibility.public -> Status.Visibility.public
            StatusesRequest.Visibility.unlisted -> Status.Visibility.unlisted
            StatusesRequest.Visibility.private -> Status.Visibility.private
            StatusesRequest.Visibility.direct -> Status.Visibility.direct
            null -> Status.Visibility.public
        }

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
            MediaAttachment(
                id = it.id.toString(),
                type = when (it.type) {
                    FileType.Image -> MediaAttachment.Type.image
                    FileType.Video -> MediaAttachment.Type.video
                    FileType.Audio -> MediaAttachment.Type.audio
                    FileType.Unknown -> MediaAttachment.Type.unknown
                },
                url = it.url,
                previewUrl = it.thumbnailUrl,
                remoteUrl = it.remoteUrl,
                description = "",
                blurhash = it.blurHash,
                textUrl = it.url
            )
        }

        Status(
            id = post.id.toString(),
            uri = post.apId,
            createdAt = Instant.ofEpochMilli(post.createdAt).toString(),
            account = account,
            content = post.text,
            visibility = postVisibility,
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
}
