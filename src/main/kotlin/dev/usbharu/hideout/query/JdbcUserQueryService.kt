package dev.usbharu.hideout.query

import dev.usbharu.hideout.domain.model.hideout.entity.User
import dev.usbharu.hideout.util.OffsetBasedPageRequest
import org.springframework.data.domain.Pageable
import org.springframework.data.repository.Repository

@org.springframework.stereotype.Repository
interface JdbcUserQueryService : Repository<User, Long> {
    fun findAllBy(pageable: Pageable): List<User>
    fun findById(id: Long): User
    fun findByName(name: String): List<User>
    fun findByNameAndDomain(name: String, domain: String): User
    fun findByUrl(url: String): User
    fun findByIdIn(ids: List<Long>): List<User>
    fun existsByNameAndDomain(name: String, domain: String): Boolean
}


class JdbcUserQueryServiceWrapper(private val jdbcUserQueryService: JdbcUserQueryService) : UserQueryService {
    override suspend fun findAll(limit: Int, offset: Long): List<User> =
        jdbcUserQueryService.findAllBy(OffsetBasedPageRequest(limit, offset.toInt()))

    override suspend fun findById(id: Long): User {
        return jdbcUserQueryService.findById(id)
    }

    override suspend fun findByName(name: String): List<User> {
        return jdbcUserQueryService.findByName(name)
    }

    override suspend fun findByNameAndDomain(name: String, domain: String): User {
        return jdbcUserQueryService.findByNameAndDomain(name, domain)
    }

    override suspend fun findByUrl(url: String): User {
        return jdbcUserQueryService.findByUrl(url)
    }

    override suspend fun findByIds(ids: List<Long>): List<User> {
        return jdbcUserQueryService.findByIdIn(ids)
    }

    override suspend fun existByNameAndDomain(name: String, domain: String): Boolean {
        return jdbcUserQueryService.existsByNameAndDomain(name, domain)
    }

}
