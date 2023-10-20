package dev.usbharu.hideout.query

import dev.usbharu.hideout.domain.model.hideout.entity.User
import org.springframework.stereotype.Repository

@Repository
interface UserQueryService {
    suspend fun findAll(limit: Int, offset: Long): List<User>
    suspend fun findById(id: Long): User
    suspend fun findByName(name: String): List<User>
    suspend fun findByNameAndDomain(name: String, domain: String): User
    suspend fun findByUrl(url: String): User
    suspend fun findByIds(ids: List<Long>): List<User>
    suspend fun existByNameAndDomain(name: String, domain: String): Boolean
    suspend fun findByKeyId(keyId: String): User
}
