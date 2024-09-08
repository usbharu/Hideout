package dev.usbharu.hideout.core.application.reaction

import dev.usbharu.hideout.core.application.exception.PermissionDeniedException
import dev.usbharu.hideout.core.application.shared.LocalUserAbstractApplicationService
import dev.usbharu.hideout.core.application.shared.Transaction
import dev.usbharu.hideout.core.domain.model.emoji.CustomEmojiRepository
import dev.usbharu.hideout.core.domain.model.emoji.UnicodeEmoji
import dev.usbharu.hideout.core.domain.model.post.PostId
import dev.usbharu.hideout.core.domain.model.post.PostRepository
import dev.usbharu.hideout.core.domain.model.reaction.Reaction
import dev.usbharu.hideout.core.domain.model.reaction.ReactionId
import dev.usbharu.hideout.core.domain.model.reaction.ReactionRepository
import dev.usbharu.hideout.core.domain.model.support.principal.LocalUser
import dev.usbharu.hideout.core.domain.service.emoji.UnicodeEmojiService
import dev.usbharu.hideout.core.domain.service.post.IPostReadAccessControl
import dev.usbharu.hideout.core.domain.shared.id.IdGenerateService
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.time.Instant

@Service
class UserCreateReactionApplicationService(
    transaction: Transaction,
    private val idGenerateService: IdGenerateService,
    private val reactionRepository: ReactionRepository,
    private val postReadAccessControl: IPostReadAccessControl,
    private val postRepository: PostRepository,
    private val customEmojiRepository: CustomEmojiRepository,
    private val unicodeEmojiService: UnicodeEmojiService
) :
    LocalUserAbstractApplicationService<CreateReaction, Unit>(
        transaction, logger
    ) {
    override suspend fun internalExecute(command: CreateReaction, principal: LocalUser) {

        val postId = PostId(command.postId)
        val post = postRepository.findById(postId) ?: throw IllegalArgumentException("Post $postId not found.")
        if (postReadAccessControl.isAllow(post, principal).not()) {
            throw PermissionDeniedException()
        }

        val customEmoji = command.customEmojiId?.let { customEmojiRepository.findById(it) }

        val unicodeEmoji = if (unicodeEmojiService.isUnicodeEmoji(command.unicodeEmoji)) {
            command.unicodeEmoji
        } else {
            "❤"
        }

        val reaction = Reaction.create(
            id = ReactionId(idGenerateService.generateId()),
            postId = postId,
            actorId = principal.actorId,
            customEmojiId = customEmoji?.id,
            unicodeEmoji = UnicodeEmoji(unicodeEmoji),
            createdAt = Instant.now()
        )

        reactionRepository.save(reaction)
    }

    companion object {
        private val logger = LoggerFactory.getLogger(UserCreateReactionApplicationService::class.java)
    }
}