package dev.usbharu.hideout.service.reaction

import dev.usbharu.hideout.domain.model.hideout.dto.Account
import dev.usbharu.hideout.domain.model.hideout.dto.ReactionResponse
import dev.usbharu.hideout.domain.model.hideout.entity.Reaction
import dev.usbharu.hideout.query.ReactionQueryService
import dev.usbharu.hideout.repository.ReactionRepository
import dev.usbharu.hideout.repository.Reactions
import dev.usbharu.hideout.repository.Users
import dev.usbharu.hideout.service.activitypub.ActivityPubReactionService
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.leftJoin
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import org.koin.core.annotation.Single

@Single
class ReactionServiceImpl(
    private val reactionRepository: ReactionRepository,
    private val activityPubReactionService: ActivityPubReactionService,
    private val reactionQueryService: ReactionQueryService
) : IReactionService {
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
            activityPubReactionService.reaction(reaction)
        }
    }

    override suspend fun removeReaction(userId: Long, postId: Long) {
        reactionQueryService.deleteByPostIdAndUserId(postId, userId)
    }

    override suspend fun findByPostIdForUser(postId: Long, userId: Long): List<ReactionResponse> {
        return newSuspendedTransaction {
            Reactions
                .leftJoin(Users, onColumn = { Reactions.userId }, otherColumn = { id })
                .select { Reactions.postId.eq(postId) }
                .groupBy { resultRow: ResultRow -> ReactionResponse("❤", true, "", listOf()) }
                .map { entry: Map.Entry<ReactionResponse, List<ResultRow>> ->
                    entry.key.copy(accounts = entry.value.map { Account(it[Users.screenName], "", it[Users.url]) })
                }
        }
    }
}
