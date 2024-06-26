package dev.usbharu.hideout.core.application.domainevent.subscribers

import dev.usbharu.hideout.core.domain.shared.domainevent.DomainEvent
import dev.usbharu.hideout.core.domain.shared.domainevent.DomainEventBody

interface DomainEventSubscriber {
    fun <T : DomainEventBody> subscribe(eventName: String, domainEventConsumer: DomainEventConsumer<T>)
}

typealias DomainEventConsumer<T> = (DomainEvent<T>) -> Unit
