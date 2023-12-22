package dev.usbharu.hideout.core.service.reaction

import dev.usbharu.hideout.activitypub.service.activity.like.APReactionService
import dev.usbharu.hideout.core.domain.model.emoji.UnicodeEmoji
import dev.usbharu.hideout.core.domain.model.reaction.Reaction
import dev.usbharu.hideout.core.domain.model.reaction.ReactionRepository
import org.jetbrains.exposed.exceptions.ExposedSQLException
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service

@Service
class ReactionServiceImpl(
    private val reactionRepository: ReactionRepository,
    private val apReactionService: APReactionService
) : ReactionService {
    override suspend fun receiveReaction(name: String, domain: String, actorId: Long, postId: Long) {
        if (reactionRepository.existByPostIdAndActorIdAndEmojiId(postId, actorId, 0).not()) {
            try {
                reactionRepository.save(
                    Reaction(reactionRepository.generateId(), UnicodeEmoji("❤"), postId, actorId)
                )
            } catch (_: ExposedSQLException) {
            }
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

    override suspend fun sendReaction(name: String, actorId: Long, postId: Long) {
        val findByPostIdAndUserIdAndEmojiId =
            reactionRepository.findByPostIdAndActorIdAndEmojiId(postId, actorId, 0)

        if (findByPostIdAndUserIdAndEmojiId != null) {
            apReactionService.removeReaction(findByPostIdAndUserIdAndEmojiId)
            reactionRepository.delete(findByPostIdAndUserIdAndEmojiId)
        }

        val reaction = Reaction(reactionRepository.generateId(), UnicodeEmoji("❤"), postId, actorId)
        reactionRepository.save(reaction)
        apReactionService.reaction(reaction)
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
