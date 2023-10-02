package dev.usbharu.hideout.query

import dev.usbharu.hideout.domain.model.hideout.dto.Account
import dev.usbharu.hideout.domain.model.hideout.dto.ReactionResponse
import dev.usbharu.hideout.domain.model.hideout.entity.Reaction
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.springframework.data.repository.Repository
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.jdbc.core.ResultSetExtractor
import java.sql.ResultSet

interface JdbcReactionQueryService : Repository<Reaction, Long> {
    fun findByPostId(postId: Long): List<Reaction>
    fun findByPostIdAndUserIdAAndEmojiId(postId: Long, userId: Long, emojiId: Long): Reaction
    fun existsByPostIdAndUserId(postId: Long, userId: Long): Boolean
}


class JdbcReactionQueryServiceWrapper(
    private val jdbcReactionQueryService: JdbcReactionQueryService,
    private val jdbcTemplate: JdbcTemplate
) : ReactionQueryService {
    override suspend fun findByPostId(postId: Long, userId: Long?): List<Reaction> {
        return jdbcReactionQueryService.findByPostId(postId)
    }

    override suspend fun findByPostIdAndUserIdAndEmojiId(postId: Long, userId: Long, emojiId: Long): Reaction {
        return findByPostIdAndUserIdAndEmojiId(postId, userId, emojiId)
    }

    override suspend fun reactionAlreadyExist(postId: Long, userId: Long, emojiId: Long): Boolean {
        return reactionAlreadyExist(postId, userId, emojiId)
    }

    override suspend fun deleteByPostIdAndUserId(postId: Long, userId: Long) {
        return deleteByPostIdAndUserId(postId, userId)
    }

    override suspend fun findByPostIdWithUsers(postId: Long, userId: Long?): List<ReactionResponse> {

        return listOfNotNull(withContext(Dispatchers.IO) {
            jdbcTemplate.query(
                """SELECT *
FROM REACTIONS
         join USERS U on U.ID = REACTIONS.USER_ID
where POST_ID = ?""", ReactionResponseRowMapper()
            )
        })
    }

    class ReactionResponseRowMapper : ResultSetExtractor<ReactionResponse> {
        override fun extractData(rs: ResultSet): ReactionResponse {
            val accounts = mutableListOf<Account>()
            while (rs.next()) {
                accounts.add(
                    Account(
                        rs.getString("SCREEN_NAME"),
                        "",
                        rs.getString("URL")
                    )
                )
            }
            return ReactionResponse("❤", true, "", accounts)
        }
    }

}
