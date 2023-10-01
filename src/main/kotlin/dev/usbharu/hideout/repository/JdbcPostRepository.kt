package dev.usbharu.hideout.repository

import dev.usbharu.hideout.domain.model.hideout.entity.Post
import dev.usbharu.hideout.exception.FailedToGetResourcesException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.springframework.context.annotation.Primary
import org.springframework.data.repository.CrudRepository
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Repository

@Repository
@Primary
class JdbcPostRepositoryWrapper(private val jdbcPostRepository: JdbcPostRepository) : PostRepository {
    override suspend fun save(post: Post): Post = withContext(Dispatchers.IO) {
        jdbcPostRepository.save(post)
    }

    override suspend fun delete(id: Long) = withContext(Dispatchers.IO) {
        jdbcPostRepository.deleteById(id)
    }

    override suspend fun findById(id: Long): Post {
        return jdbcPostRepository.findByIdOrNull(id) ?: throw FailedToGetResourcesException("id: $id was not found.")
    }
}

@Repository
interface JdbcPostRepository : CrudRepository<Post, Long>
