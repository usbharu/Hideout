package dev.usbharu.hideout.core.domain.model.reaction

import dev.usbharu.hideout.core.domain.model.post.PostId

interface ReactionRepository {
    suspend fun save(reaction: Reaction): Reaction
    suspend fun findById(reactionId: ReactionId): Reaction?
    suspend fun findByPostId(postId: PostId): List<Reaction>
    suspend fun delete(reaction: Reaction)
}