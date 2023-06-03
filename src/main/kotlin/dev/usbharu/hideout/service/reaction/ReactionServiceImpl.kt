package dev.usbharu.hideout.service.reaction

import dev.usbharu.hideout.domain.model.hideout.entity.Reaction
import dev.usbharu.hideout.repository.ReactionRepository
import org.koin.core.annotation.Single

@Single
class ReactionServiceImpl(private val reactionRepository: ReactionRepository) : IReactionService {
    override suspend fun receiveReaction(name: String, domain: String, userId: Long, postId: Long) {
        reactionRepository.save(
                Reaction(
                        reactionRepository.generateId(),
                        0,
                        postId, userId
                )
        )
    }
}
