package dev.usbharu.hideout.query

import dev.usbharu.hideout.domain.model.hideout.entity.Post
import org.springframework.context.annotation.Primary
import org.springframework.stereotype.Repository

@Repository
interface JdbcPostQueryService : org.springframework.data.repository.Repository<Post, Long> {
    fun findById(id: Long): Post
    fun findByUrl(url: String): Post
    fun findByApId(apId: String): Post
}

@Repository
@Primary
class JdbcPostQueryServiceWrapper(private val jdbcPostQueryService: JdbcPostQueryService) : PostQueryService {
    override suspend fun findById(id: Long): Post {
        return jdbcPostQueryService.findById(id)
    }

    override suspend fun findByUrl(url: String): Post {
        return jdbcPostQueryService.findByUrl(url)
    }

    override suspend fun findByApId(string: String): Post {
        return jdbcPostQueryService.findByApId(string)
    }
}
