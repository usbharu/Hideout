package dev.usbharu.hideout.query

import dev.usbharu.hideout.domain.model.hideout.dto.Account
import dev.usbharu.hideout.domain.model.hideout.dto.ReactionResponse
import dev.usbharu.hideout.domain.model.hideout.entity.Reaction
import dev.usbharu.hideout.exception.FailedToGetResourcesException
import dev.usbharu.hideout.repository.Reactions
import dev.usbharu.hideout.repository.Users
import dev.usbharu.hideout.repository.toReaction
import dev.usbharu.hideout.util.singleOr
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.koin.core.annotation.Single

@Single
class ReactionQueryServiceImpl : ReactionQueryService {
    override suspend fun findByPostId(postId: Long, userId: Long?): List<Reaction> {
        return Reactions.select {
            Reactions.postId.eq(postId)
        }.map { it.toReaction() }
    }

    @Suppress("FunctionMaxLength")
    override suspend fun findByPostIdAndUserIdAndEmojiId(postId: Long, userId: Long, emojiId: Long): Reaction {
        return Reactions
            .select {
                Reactions.postId.eq(postId).and(Reactions.userId.eq(userId)).and(
                    Reactions.emojiId.eq(emojiId)
                )
            }
            .singleOr {
                FailedToGetResourcesException(
                    "postId: $postId,userId: $userId,emojiId: $emojiId is duplicate or does not exist.",
                    it
                )
            }
            .toReaction()
    }

    override suspend fun reactionAlreadyExist(postId: Long, userId: Long, emojiId: Long): Boolean {
        return Reactions.select {
            Reactions.postId.eq(postId).and(Reactions.userId.eq(userId)).and(
                Reactions.emojiId.eq(emojiId)
            )
        }.empty().not()
    }

    override suspend fun deleteByPostIdAndUserId(postId: Long, userId: Long) {
        Reactions.deleteWhere { Reactions.postId.eq(postId).and(Reactions.userId.eq(userId)) }
    }

    override suspend fun findByPostIdWithUsers(postId: Long, userId: Long?): List<ReactionResponse> {
        return Reactions
            .leftJoin(Users, onColumn = { Reactions.userId }, otherColumn = { id })
            .select { Reactions.postId.eq(postId) }
            .groupBy { _: ResultRow -> ReactionResponse("❤", true, "", emptyList()) }
            .map { entry: Map.Entry<ReactionResponse, List<ResultRow>> ->
                entry.key.copy(accounts = entry.value.map { Account(it[Users.screenName], "", it[Users.url]) })
            }
    }
}
