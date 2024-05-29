package dev.usbharu.hideout.core.domain.shared.domainevent

interface DomainEventPublisher {
    suspend fun publishEvent(domainEvent: DomainEvent)
}