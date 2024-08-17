package dev.usbharu.hideout.core.infrastructure.springframework.domainevent

import dev.usbharu.hideout.core.application.domainevent.subscribers.DomainEventConsumer
import dev.usbharu.hideout.core.application.domainevent.subscribers.DomainEventSubscriber
import dev.usbharu.hideout.core.domain.shared.domainevent.DomainEvent
import dev.usbharu.hideout.core.domain.shared.domainevent.DomainEventBody
import org.slf4j.LoggerFactory
import org.springframework.context.event.EventListener
import org.springframework.stereotype.Component

@Component
class SpringFrameworkDomainEventSubscriber : DomainEventSubscriber {

    val map = mutableMapOf<String, MutableList<DomainEventConsumer<*>>>()

    override fun <T : DomainEventBody> subscribe(eventName: String, domainEventConsumer: DomainEventConsumer<T>) {
        map.getOrPut(eventName) { mutableListOf() }.add(domainEventConsumer as DomainEventConsumer<*>)
    }

    @EventListener
    suspend fun onDomainEventPublished(domainEvent: DomainEvent<*>) {
        logger.trace("Domain Event Published: $domainEvent")
        map[domainEvent.name]?.forEach {
            try {
                it.invoke(domainEvent)
            } catch (e: Exception) {
                logger.error("", e)
            }
        }
    }

    companion object {
        private val logger = LoggerFactory.getLogger(SpringFrameworkDomainEventSubscriber::class.java)
    }
}
