package dev.usbharu.hideout.core.query

import dev.usbharu.hideout.core.domain.model.actor.Actor
import org.springframework.stereotype.Repository

@Repository
interface ActorQueryService {
    suspend fun findAll(limit: Int, offset: Long): List<Actor>
    suspend fun findById(id: Long): Actor
    suspend fun findByName(name: String): List<Actor>
    suspend fun findByNameAndDomain(name: String, domain: String): Actor
    suspend fun findByUrl(url: String): Actor
    suspend fun findByIds(ids: List<Long>): List<Actor>
    suspend fun existByNameAndDomain(name: String, domain: String): Boolean
    suspend fun findByKeyId(keyId: String): Actor
}
