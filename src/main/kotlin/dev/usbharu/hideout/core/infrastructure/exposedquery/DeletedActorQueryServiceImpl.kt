package dev.usbharu.hideout.core.infrastructure.exposedquery

import dev.usbharu.hideout.core.domain.model.deletedActor.DeletedActor
import dev.usbharu.hideout.core.infrastructure.exposedrepository.DeletedActors
import dev.usbharu.hideout.core.infrastructure.exposedrepository.toDeletedActor
import dev.usbharu.hideout.core.query.DeletedActorQueryService
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.select
import org.springframework.stereotype.Repository

@Repository
class DeletedActorQueryServiceImpl : DeletedActorQueryService {
    override suspend fun findByNameAndDomain(name: String, domain: String): DeletedActor? {
        return DeletedActors
            .select { DeletedActors.name eq name and (DeletedActors.domain eq domain) }
            .singleOrNull()
            ?.toDeletedActor()
    }
}
