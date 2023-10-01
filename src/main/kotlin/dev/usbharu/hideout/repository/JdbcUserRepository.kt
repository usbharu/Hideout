package dev.usbharu.hideout.repository

import dev.usbharu.hideout.domain.model.hideout.entity.User
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.springframework.context.annotation.Primary
import org.springframework.data.repository.CrudRepository
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Repository

@Repository
interface JdbcUserRepository : CrudRepository<User, Long> {
}


@Repository
@Primary
class JdbcUserRepositoryWrapper(private val jdbcUserRepository: JdbcUserRepository) : UserRepository {
    override suspend fun save(user: User): User {
        return withContext(Dispatchers.IO) {
            jdbcUserRepository.save(user)
        }
    }

    override suspend fun findById(id: Long): User? {
        return jdbcUserRepository.findByIdOrNull(id)
    }

    override suspend fun delete(id: Long) {
        withContext(Dispatchers.IO) {
            jdbcUserRepository.deleteById(id)
        }
    }

    override suspend fun deleteFollowRequest(id: Long, follower: Long) {

    }

    override suspend fun findFollowRequestsById(id: Long, follower: Long): Boolean {
        return true
    }

    override suspend fun nextId(): Long {
        TODO("Not yet implemented")
    }
}
