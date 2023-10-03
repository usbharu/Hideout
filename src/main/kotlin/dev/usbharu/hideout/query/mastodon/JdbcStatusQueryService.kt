package dev.usbharu.hideout.query.mastodon

import dev.usbharu.hideout.domain.mastodon.model.generated.Status
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.jdbc.core.ResultSetExtractor
import java.sql.ResultSet

class JdbcStatusQueryService(private val jdbcTemplate: JdbcTemplate) : StatusQueryService {
    override suspend fun findByPostIds(ids: List<Long>): List<Status> {
        //language=H2
        jdbcTemplate.query("SELECT *\nfrom POSTS\n         join USERS on POSTS.\"userId\" = USERS.ID\nWHERE ")
    }

    class StatusResultSetExtractor : ResultSetExtractor<List<Status>> {
        override fun extractData(rs: ResultSet): List<Status>? {

        }
    }
}
