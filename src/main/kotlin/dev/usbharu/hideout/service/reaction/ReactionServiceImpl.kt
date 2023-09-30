package dev.usbharu.hideout.service.reaction

import dev.usbharu.hideout.domain.model.hideout.entity.Reaction
import dev.usbharu.hideout.query.ReactionQueryService
import dev.usbharu.hideout.repository.ReactionRepository
import dev.usbharu.hideout.service.ap.APReactionService
import dev.usbharu.hideout.service.core.IdGenerateService
import org.springframework.stereotype.Service

@Service
class ReactionServiceImpl(
    private val reactionRepository: ReactionRepository,
    private val apReactionService: APReactionService,
    private val reactionQueryService: ReactionQueryService,
    private val idGenerateService: IdGenerateService
) : ReactionService {
    override suspend fun receiveReaction(name: String, domain: String, userId: Long, postId: Long) {
        if (reactionQueryService.reactionAlreadyExist(postId, userId, 0).not()) {
            reactionRepository.save(
                Reaction(idGenerateService.generateId(), 0, postId, userId)
            )
        }
    }

    override suspend fun sendReaction(name: String, userId: Long, postId: Long) {
        if (reactionQueryService.reactionAlreadyExist(postId, userId, 0)) {
            // delete
            reactionQueryService.deleteByPostIdAndUserId(postId, userId)
        } else {
            val reaction = Reaction(idGenerateService.generateId(), 0, postId, userId)
            reactionRepository.save(reaction)
            apReactionService.reaction(reaction)
        }
    }

    override suspend fun removeReaction(userId: Long, postId: Long) {
        reactionQueryService.deleteByPostIdAndUserId(postId, userId)
    }
}
