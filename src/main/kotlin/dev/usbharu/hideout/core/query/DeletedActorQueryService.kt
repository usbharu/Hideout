package dev.usbharu.hideout.core.query

import dev.usbharu.hideout.core.domain.model.deletedActor.DeletedActor

interface DeletedActorQueryService {
    suspend fun findByNameAndDomain(name: String, domain: String): DeletedActor?
}
