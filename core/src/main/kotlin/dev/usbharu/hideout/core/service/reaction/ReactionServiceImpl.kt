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

package dev.usbharu.hideout.core.service.reaction

import dev.usbharu.hideout.activitypub.service.activity.like.APReactionService
import dev.usbharu.hideout.core.domain.exception.resource.DuplicateException
import dev.usbharu.hideout.core.domain.model.emoji.Emoji
import dev.usbharu.hideout.core.domain.model.post.PostRepository
import dev.usbharu.hideout.core.domain.model.reaction.Reaction
import dev.usbharu.hideout.core.domain.model.reaction.ReactionRepository
import dev.usbharu.hideout.core.service.notification.NotificationService
import dev.usbharu.hideout.core.service.notification.ReactionNotificationRequest
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service

@Service
class ReactionServiceImpl(
    private val reactionRepository: ReactionRepository,
    private val apReactionService: APReactionService,
    private val notificationService: NotificationService,
    private val postRepository: PostRepository
) : ReactionService {
    override suspend fun receiveReaction(
        emoji: Emoji,
        actorId: Long,
        postId: Long
    ) {
        if (reactionRepository.existByPostIdAndActor(postId, actorId)) {
            reactionRepository.deleteByPostIdAndActorId(postId, actorId)
        }
        try {
            val reaction = reactionRepository.save(Reaction(reactionRepository.generateId(), emoji, postId, actorId))

            notificationService.publishNotify(
                ReactionNotificationRequest(
                    postRepository.findById(postId)!!.actorId,
                    actorId,
                    postId,
                    reaction.id
                )
            )
        } catch (_: DuplicateException) {
        }
    }

    override suspend fun receiveRemoveReaction(actorId: Long, postId: Long) {
        val reaction = reactionRepository.findByPostIdAndActorIdAndEmojiId(postId, actorId, 0)
        if (reaction == null) {
            LOGGER.warn("FAILED receive Remove Reaction. $actorId $postId")
            return
        }
        reactionRepository.delete(reaction)
    }

    override suspend fun sendReaction(emoji: Emoji, actorId: Long, postId: Long) {
        val findByPostIdAndUserIdAndEmojiId =
            reactionRepository.findByPostIdAndActorIdAndEmojiId(postId, actorId, 0)

        if (findByPostIdAndUserIdAndEmojiId != null) {
            apReactionService.removeReaction(findByPostIdAndUserIdAndEmojiId)
            reactionRepository.delete(findByPostIdAndUserIdAndEmojiId)
        }

        val reaction = Reaction(reactionRepository.generateId(), emoji, postId, actorId)
        reactionRepository.save(reaction)
        apReactionService.reaction(reaction)

        val id = postRepository.findById(postId)!!.actorId

        notificationService.publishNotify(ReactionNotificationRequest(id, actorId, postId, reaction.id))
    }

    override suspend fun removeReaction(actorId: Long, postId: Long) {
        val findByPostIdAndUserIdAndEmojiId =
            reactionRepository.findByPostIdAndActorIdAndEmojiId(postId, actorId, 0)
        if (findByPostIdAndUserIdAndEmojiId == null) {
            LOGGER.warn("FAILED Remove reaction. actorId: $actorId postId: $postId")
            return
        }
        reactionRepository.delete(findByPostIdAndUserIdAndEmojiId)
        apReactionService.removeReaction(findByPostIdAndUserIdAndEmojiId)
    }

    companion object {
        val LOGGER: Logger = LoggerFactory.getLogger(ReactionServiceImpl::class.java)
    }
}
