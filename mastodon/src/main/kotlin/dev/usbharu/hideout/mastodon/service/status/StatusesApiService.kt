/*
 * Copyright (C) 2024 usbharu
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package dev.usbharu.hideout.mastodon.service.status

import dev.usbharu.hideout.activitypub.service.objects.emoji.EmojiService
import dev.usbharu.hideout.application.external.Transaction
import dev.usbharu.hideout.core.domain.model.actor.ActorRepository
import dev.usbharu.hideout.core.domain.model.emoji.UnicodeEmoji
import dev.usbharu.hideout.core.domain.model.media.MediaRepository
import dev.usbharu.hideout.core.domain.model.media.toMediaAttachments
import dev.usbharu.hideout.core.domain.model.post.PostRepository
import dev.usbharu.hideout.core.domain.model.relationship.RelationshipRepository
import dev.usbharu.hideout.core.service.post.PostCreateDto
import dev.usbharu.hideout.core.service.post.PostService
import dev.usbharu.hideout.core.service.reaction.ReactionService
import dev.usbharu.hideout.domain.mastodon.model.generated.Status
import dev.usbharu.hideout.domain.mastodon.model.generated.Status.Visibility.*
import dev.usbharu.hideout.mastodon.interfaces.api.status.StatusesRequest
import dev.usbharu.hideout.mastodon.interfaces.api.status.toPostVisibility
import dev.usbharu.hideout.mastodon.interfaces.api.status.toStatusVisibility
import dev.usbharu.hideout.mastodon.query.StatusQueryService
import dev.usbharu.hideout.mastodon.service.account.AccountService
import dev.usbharu.hideout.util.EmojiUtil
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.time.Instant

@Service
interface StatusesApiService {
    suspend fun postStatus(
        statusesRequest: StatusesRequest,
        userId: Long
    ): Status

    suspend fun findById(
        id: Long,
        userId: Long?
    ): Status?

    suspend fun emojiReactions(
        postId: Long,
        userId: Long,
        emojiName: String
    ): Status?

    suspend fun removeEmojiReactions(
        postId: Long,
        userId: Long,
        emojiName: String
    ): Status?
}

@Service
@Suppress("LongParameterList")
class StatsesApiServiceImpl(
    private val postService: PostService,
    private val accountService: AccountService,
    private val mediaRepository: MediaRepository,
    private val transaction: Transaction,
    private val actorRepository: ActorRepository,
    private val postRepository: PostRepository,
    private val statusQueryService: StatusQueryService,
    private val relationshipRepository: RelationshipRepository,
    private val reactionService: ReactionService,
    private val emojiService: EmojiService
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
            val findById = postRepository.findById(post.replyId)
            if (findById == null) {
                null
            } else {
                actorRepository.findById(findById.actorId)?.id
            }
        } else {
            null
        }

        // TODO: n+1解消
        val mediaAttachment = post.mediaIds.mapNotNull { mediaId ->
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

    override suspend fun findById(id: Long, userId: Long?): Status? {
        val status = statusQueryService.findByPostId(id)

        return status(status, userId)
    }

    private suspend fun status(
        status: Status,
        userId: Long?
    ): Status? {
        return when (status.visibility) {
            public -> status
            unlisted -> status
            private -> {
                if (userId == null) {
                    return null
                }

                val relationship =
                    relationshipRepository.findByUserIdAndTargetUserId(userId, status.account.id.toLong())
                        ?: return null
                if (relationship.following) {
                    return status
                }
                return null
            }

            direct -> null
        }
    }

    override suspend fun emojiReactions(postId: Long, userId: Long, emojiName: String): Status? {
        status(statusQueryService.findByPostId(postId), userId) ?: return null

        val emoji = try {
            if (EmojiUtil.isEmoji(emojiName)) {
                UnicodeEmoji(emojiName)
            } else {
                emojiService.findByEmojiName(emojiName)!!
            }
        } catch (_: IllegalStateException) {
            UnicodeEmoji("❤")
        } catch (_: NullPointerException) {
            UnicodeEmoji("❤")
        }
        reactionService.sendReaction(emoji, userId, postId)
        return statusQueryService.findByPostId(postId)
    }

    override suspend fun removeEmojiReactions(postId: Long, userId: Long, emojiName: String): Status? {
        reactionService.removeReaction(userId, postId)

        return status(statusQueryService.findByPostId(postId), userId)
    }

    companion object {
        private val logger = LoggerFactory.getLogger(StatusesApiService::class.java)
    }
}
