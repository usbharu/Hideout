package dev.usbharu.hideout.service.reaction

import dev.usbharu.hideout.domain.model.hideout.entity.Reaction
import dev.usbharu.hideout.repository.ReactionRepository
import dev.usbharu.hideout.service.activitypub.ActivityPubReactionService
import org.koin.core.annotation.Single

@Single
class ReactionServiceImpl(
    private val reactionRepository: ReactionRepository,
    private val activityPubReactionService: ActivityPubReactionService
) : IReactionService {
    override suspend fun receiveReaction(name: String, domain: String, userId: Long, postId: Long) {
        if (reactionRepository.reactionAlreadyExist(postId, userId, 0).not()) {
            reactionRepository.save(
                Reaction(reactionRepository.generateId(), 0, postId, userId)
            )
        }
    }

    override suspend fun sendReaction(name: String, userId: Long, postId: Long) {
        if (reactionRepository.reactionAlreadyExist(postId, userId, 0)) {
            //delete
        } else {
            val reaction = Reaction(reactionRepository.generateId(), 0, postId, userId)
            reactionRepository.save(reaction)
            activityPubReactionService.reaction(reaction)
        }
    }
}
