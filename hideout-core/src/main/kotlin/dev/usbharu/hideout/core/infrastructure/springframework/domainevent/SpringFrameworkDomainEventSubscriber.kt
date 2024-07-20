package dev.usbharu.hideout.core.infrastructure.springframework.domainevent

import dev.usbharu.hideout.core.application.domainevent.subscribers.DomainEventConsumer
import dev.usbharu.hideout.core.application.domainevent.subscribers.DomainEventSubscriber
import dev.usbharu.hideout.core.domain.shared.domainevent.DomainEvent
import dev.usbharu.hideout.core.domain.shared.domainevent.DomainEventBody
import org.springframework.context.event.EventListener
import org.springframework.stereotype.Component

@Component
class SpringFrameworkDomainEventSubscriber : DomainEventSubscriber {

    val map = mutableMapOf<String, MutableList<DomainEventConsumer<*>>>()

    override fun <T : DomainEventBody> subscribe(eventName: String, domainEventConsumer: DomainEventConsumer<T>) {
        map.getOrPut(eventName) { mutableListOf() }.add(domainEventConsumer as DomainEventConsumer<*>)
    }

    @EventListener
    fun onDomainEventPublished(domainEvent: DomainEvent<*>) {
        map[domainEvent.name]?.forEach {
            try {
                it.invoke(domainEvent)
            } catch (e: Exception) {
            }
        }
    }
}
