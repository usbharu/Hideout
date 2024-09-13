package dev.usbharu.hideout.core.application.domainevent.subscribers

import dev.usbharu.hideout.core.domain.shared.domainevent.DomainEvent
import dev.usbharu.hideout.core.domain.shared.domainevent.DomainEventBody

interface DomainEventSubscriber {
    fun <T : DomainEventBody> subscribe(eventName: String, domainEventConsumer: DomainEventConsumer<T>)
    fun getSubscribers(): Map<String, List<DomainEventConsumer<*>>>
}

typealias DomainEventConsumer<T> = suspend (DomainEvent<T>) -> Unit
