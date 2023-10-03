package dev.usbharu.hideout.query

import dev.usbharu.hideout.domain.model.hideout.dto.PostResponse
import dev.usbharu.hideout.domain.model.hideout.dto.UserResponse
import dev.usbharu.hideout.domain.model.hideout.entity.Visibility
import org.springframework.context.annotation.Primary
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.jdbc.core.RowMapper
import org.springframework.stereotype.Repository
import java.sql.ResultSet

@Repository
@Primary
class JdbcPostResponseQueryService(private val jdbcTemplate: JdbcTemplate) : PostResponseQueryService {
    override suspend fun findById(id: Long, userId: Long?): PostResponse {
        return jdbcTemplate.query(
            """SELECT POSTS.ID,
       POSTS.OVERVIEW,
       POSTS.TEXT,
       "createdAt",
       POSTS.VISIBILITY,
       POSTS.URL,
       POSTS.SENSITIVE,
       USERS.ID,
       USERS.NAME,
       USERS.DOMAIN,
       USERS.SCREEN_NAME,
       USERS.DESCRIPTION,
       USERS.URL,
       USERS.CREATED_AT
FROM POSTS
         join PUBLIC.USERS on USERS.ID = POSTS."userId"
where POSTS.ID = ?""", PostResponseRowMapper()
        ).single()
    }

    class PostResponseRowMapper : RowMapper<PostResponse> {
        override fun mapRow(rs: ResultSet, rowNum: Int): PostResponse {
            return PostResponse(
                rs.getLong("POSTS.ID").toString(),
                UserResponse(
                    rs.getString("USERS.ID"),
                    rs.getString("USERS.NAME"),
                    rs.getString("USERS.DOMAIN"),
                    rs.getString("USERS.SCREEN_NAME"),
                    rs.getString("USERS.DESCRIPTION"),
                    rs.getString("USERS.URL"),
                    rs.getLong("USERS.CREATED_AT")
                ),
                rs.getString("POSTS.OVERVIEW"),
                rs.getString("POSTS.TEXT"),
                rs.getLong("createdAt"),
                Visibility.values().first { visibility -> visibility.ordinal == rs.getInt("POSTS.VISIBILITY") },
                rs.getString("POSTS.URL"),
                rs.getBoolean("POSTS.SENSITIVE")
            )
        }

    }
}
