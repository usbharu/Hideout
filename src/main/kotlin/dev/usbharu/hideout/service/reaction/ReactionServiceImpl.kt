package dev.usbharu.hideout.service.reaction

import dev.usbharu.hideout.domain.model.hideout.entity.Reaction
import dev.usbharu.hideout.query.ReactionQueryService
import dev.usbharu.hideout.repository.ReactionRepository
import dev.usbharu.hideout.service.ap.APReactionService
import org.koin.core.annotation.Single

@Single
class ReactionServiceImpl(
    private val reactionRepository: ReactionRepository,
    private val apReactionService: APReactionService,
    private val reactionQueryService: ReactionQueryService
) : ReactionService {
    override suspend fun receiveReaction(name: String, domain: String, userId: Long, postId: Long) {
        if (reactionQueryService.reactionAlreadyExist(postId, userId, 0).not()) {
            reactionRepository.save(
                Reaction(reactionRepository.generateId(), 0, postId, userId)
            )
        }
    }

    override suspend fun sendReaction(name: String, userId: Long, postId: Long) {
        if (reactionQueryService.reactionAlreadyExist(postId, userId, 0)) {
            // delete
            reactionQueryService.deleteByPostIdAndUserId(postId, userId)
        } else {
            val reaction = Reaction(reactionRepository.generateId(), 0, postId, userId)
            reactionRepository.save(reaction)
            apReactionService.reaction(reaction)
        }
    }

    override suspend fun removeReaction(userId: Long, postId: Long) {
        reactionQueryService.deleteByPostIdAndUserId(postId, userId)
    }
}
