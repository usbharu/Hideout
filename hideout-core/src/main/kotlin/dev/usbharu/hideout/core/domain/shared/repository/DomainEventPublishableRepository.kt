package dev.usbharu.hideout.core.domain.shared.repository

import dev.usbharu.hideout.core.domain.shared.domainevent.DomainEventPublisher
import dev.usbharu.hideout.core.domain.shared.domainevent.DomainEventStorable
import org.springframework.stereotype.Repository

@Repository
interface DomainEventPublishableRepository<T : DomainEventStorable> {
    val domainEventPublisher: DomainEventPublisher
    suspend fun update(entity: T) {
        entity.getDomainEvents().distinctBy {
            if (it.collectable) {
                it.name
            } else {
                it.id
            }
        }.forEach {
            domainEventPublisher.publishEvent(it)
        }
        entity.clearDomainEvents()
    }
}