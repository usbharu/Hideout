package dev.usbharu.hideout.repository

import dev.usbharu.hideout.domain.model.hideout.entity.Reaction
import dev.usbharu.hideout.service.core.IdGenerateService
import org.jetbrains.exposed.dao.id.LongIdTable
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.springframework.stereotype.Repository

@Repository
class ReactionRepositoryImpl(
    private val idGenerateService: IdGenerateService
) : ReactionRepository {

    override suspend fun generateId(): Long = idGenerateService.generateId()

    override suspend fun save(reaction: Reaction): Reaction {
        if (Reactions.select { Reactions.id eq reaction.id }.empty()) {
            Reactions.insert {
                it[id] = reaction.id
                it[emojiId] = reaction.emojiId
                it[postId] = reaction.postId
                it[userId] = reaction.userId
            }
        } else {
            Reactions.update({ Reactions.id eq reaction.id }) {
                it[emojiId] = reaction.emojiId
                it[postId] = reaction.postId
                it[userId] = reaction.userId
            }
        }
        return reaction
    }

    override suspend fun delete(reaction: Reaction): Reaction {
        Reactions.deleteWhere {
            id.eq(reaction.id)
                .and(postId.eq(reaction.postId))
                .and(userId.eq(reaction.postId))
                .and(emojiId.eq(reaction.emojiId))
        }
        return reaction
    }
}

fun ResultRow.toReaction(): Reaction {
    return Reaction(
        this[Reactions.id].value,
        this[Reactions.emojiId],
        this[Reactions.postId],
        this[Reactions.userId]
    )
}

object Reactions : LongIdTable("reactions") {
    val emojiId = long("emoji_id")
    val postId = long("post_id").references(Posts.id)
    val userId = long("user_id").references(Users.id)

    init {
        uniqueIndex(emojiId, postId, userId)
    }
}
