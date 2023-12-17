package dev.usbharu.hideout.core.domain.model.actor

import org.springframework.stereotype.Repository

@Repository
interface ActorRepository {
    suspend fun save(actor: Actor): Actor

    suspend fun findById(id: Long): Actor?

    suspend fun findByIdWithLock(id: Long): Actor?

    suspend fun findAll(limit: Int, offset: Long): List<Actor>

    suspend fun findByName(name: String): List<Actor>

    suspend fun findByNameAndDomain(name: String, domain: String): Actor?

    suspend fun findByNameAndDomainWithLock(name: String, domain: String): Actor?

    suspend fun findByUrl(url: String): Actor?

    suspend fun findByUrlWithLock(url: String): Actor?

    suspend fun findByIds(ids: List<Long>): List<Actor>

    suspend fun findByKeyId(keyId: String): Actor?

    suspend fun delete(id: Long)

    suspend fun nextId(): Long
}
