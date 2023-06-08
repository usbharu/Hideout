package dev.usbharu.hideout.repository

import dev.usbharu.hideout.domain.model.hideout.entity.Reaction
import dev.usbharu.hideout.service.core.IdGenerateService
import kotlinx.coroutines.Dispatchers
import org.jetbrains.exposed.dao.id.LongIdTable
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import org.jetbrains.exposed.sql.transactions.transaction
import org.koin.core.annotation.Single

@Single
class ReactionRepositoryImpl(
    private val database: Database,
    private val idGenerateService: IdGenerateService
) : ReactionRepository {

    init {
        transaction(database) {
            SchemaUtils.create(Reactions)
            SchemaUtils.createMissingTablesAndColumns(Reactions)
        }
    }

    @Suppress("InjectDispatcher")
    suspend fun <T> query(block: suspend () -> T): T =
        newSuspendedTransaction(Dispatchers.IO) { block() }

    override suspend fun generateId(): Long = idGenerateService.generateId()

    override suspend fun save(reaction: Reaction): Reaction {
        query {
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
        }
        return reaction
    }

    override suspend fun reactionAlreadyExist(postId: Long, userId: Long, emojiId: Long): Boolean {
        return query {
            Reactions.select {
                Reactions.postId.eq(postId).and(Reactions.userId.eq(userId)).and(
                    Reactions.emojiId.eq(emojiId)
                )
            }.empty().not()
        }
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
