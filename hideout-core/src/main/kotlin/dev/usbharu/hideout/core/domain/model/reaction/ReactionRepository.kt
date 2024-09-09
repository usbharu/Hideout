package dev.usbharu.hideout.core.domain.model.reaction

import dev.usbharu.hideout.core.domain.model.actor.ActorId
import dev.usbharu.hideout.core.domain.model.emoji.CustomEmojiId
import dev.usbharu.hideout.core.domain.model.post.PostId

@Suppress("FunctionMaxLength")
interface ReactionRepository {
    suspend fun save(reaction: Reaction): Reaction
    suspend fun findById(reactionId: ReactionId): Reaction?
    suspend fun findByPostId(postId: PostId): List<Reaction>
    suspend fun existsByPostIdAndActorIdAndCustomEmojiIdOrUnicodeEmoji(
        postId: PostId,
        actorId: ActorId,
        customEmojiId: CustomEmojiId?,
        unicodeEmoji: String
    ): Boolean

    suspend fun findByPostIdAndActorIdAndCustomEmojiIdOrUnicodeEmoji(
        postId: PostId,
        actorId: ActorId,
        customEmojiId: CustomEmojiId?,
        unicodeEmoji: String
    ): Reaction?

    suspend fun delete(reaction: Reaction)
}
