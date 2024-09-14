package dev.usbharu.hideout.core.application.reaction

import dev.usbharu.hideout.core.application.shared.LocalUserAbstractApplicationService
import dev.usbharu.hideout.core.application.shared.Transaction
import dev.usbharu.hideout.core.domain.model.emoji.CustomEmojiRepository
import dev.usbharu.hideout.core.domain.model.post.PostId
import dev.usbharu.hideout.core.domain.model.reaction.ReactionRepository
import dev.usbharu.hideout.core.domain.model.support.principal.LocalUser
import dev.usbharu.hideout.core.domain.service.emoji.UnicodeEmojiService
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service

@Service
class UserRemoveReactionApplicationService(
    transaction: Transaction,
    private val customEmojiRepository: CustomEmojiRepository,
    private val reactionRepository: ReactionRepository,
    private val unicodeEmojiService: UnicodeEmojiService
) :
    LocalUserAbstractApplicationService<RemoveReaction, Unit>(
        transaction,
        logger
    ) {
    override suspend fun internalExecute(command: RemoveReaction, principal: LocalUser) {
        val postId = PostId(command.postId)

        val customEmoji = command.customEmojiId?.let { customEmojiRepository.findById(it) }

        val unicodeEmoji = if (unicodeEmojiService.isUnicodeEmoji(command.unicodeEmoji)) {
            command.unicodeEmoji
        } else {
            "❤"
        }
        val reaction =
            reactionRepository.findByPostIdAndActorIdAndCustomEmojiIdOrUnicodeEmoji(
                postId,
                principal.actorId,
                customEmoji?.id,
                unicodeEmoji
            )
                ?: throw IllegalArgumentException("Reaction $postId ${principal.actorId} ${customEmoji?.id} $unicodeEmoji not found.")

        reaction.delete()

        reactionRepository.delete(reaction)
    }

    companion object {
        private val logger = LoggerFactory.getLogger(UserRemoveReactionApplicationService::class.java)
    }
}

data class RemoveReaction(
    val postId: Long,
    val customEmojiId: Long?,
    val unicodeEmoji: String
)