package dev.usbharu.hideout.core.infrastructure.exposedrepository

import dev.usbharu.hideout.core.domain.model.actor.Actor2
import dev.usbharu.hideout.core.domain.model.actor.Actor2Repository
import dev.usbharu.hideout.core.domain.model.actor.ActorId
import dev.usbharu.hideout.core.domain.shared.domainevent.DomainEventPublisher
import dev.usbharu.hideout.core.domain.shared.repository.DomainEventPublishableRepository
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Repository

@Repository
class ExposedActor2Repository(override val domainEventPublisher: DomainEventPublisher) : AbstractRepository(),
    DomainEventPublishableRepository<Actor2>, Actor2Repository {
    override val logger: Logger
        get() = Companion.logger

    companion object {
        private val logger = LoggerFactory.getLogger(ExposedActor2Repository::class.java)
    }

    override suspend fun save(actor: Actor2): Actor2 {
        query {

        }
        update(actor)
        return actor
    }

    override suspend fun delete(actor: Actor2) {
        TODO("Not yet implemented")
    }

    override suspend fun findById(id: ActorId): Actor2? {
        TODO("Not yet implemented")
    }
}
