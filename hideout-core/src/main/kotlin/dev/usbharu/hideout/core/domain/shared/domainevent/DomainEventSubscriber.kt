package dev.usbharu.hideout.core.domain.shared.domainevent

interface DomainEventSubscriber {
    fun subscribe(eventName: String, domainEventConsumer: DomainEventConsumer)
}

typealias DomainEventConsumer = (DomainEvent) -> Unit
