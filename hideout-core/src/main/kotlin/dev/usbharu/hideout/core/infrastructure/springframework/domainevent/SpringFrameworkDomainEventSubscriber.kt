package dev.usbharu.hideout.core.infrastructure.springframework.domainevent

import dev.usbharu.hideout.core.application.domainevent.subscribers.DomainEventConsumer
import dev.usbharu.hideout.core.application.domainevent.subscribers.DomainEventSubscriber
import dev.usbharu.hideout.core.domain.shared.domainevent.DomainEventBody
import dev.usbharu.hideout.core.infrastructure.springframework.ApplicationRequestLogInterceptor.Companion.requestId
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.slf4j.MDCContext
import org.slf4j.LoggerFactory
import org.slf4j.MDC
import org.springframework.context.event.EventListener
import org.springframework.stereotype.Component

@Component
class SpringFrameworkDomainEventSubscriber : DomainEventSubscriber {

    val map = mutableMapOf<String, MutableList<DomainEventConsumer<*>>>()

    override fun <T : DomainEventBody> subscribe(eventName: String, domainEventConsumer: DomainEventConsumer<T>) {
        map.getOrPut(eventName) { mutableListOf() }.add(domainEventConsumer as DomainEventConsumer<*>)
    }

    override fun getSubscribers(): Map<String, List<DomainEventConsumer<*>>> = map

    @EventListener
    suspend fun onDomainEventPublished(domainEvent: SpringDomainEvent) {
        logger.debug(
            "Domain Event Published: {} id: {} requestId: {}",
            domainEvent.domainEvent.name,
            domainEvent.domainEvent.id,
            domainEvent.requestId
        )
        coroutineScope {
            map[domainEvent.domainEvent.name]?.map {
                async(MDCContext()) {
                    try {
                        MDC.put(requestId, domainEvent.requestId)
                        it.invoke(domainEvent.domainEvent)
                    } catch (@Suppress("TooGenericExceptionCaught") e: Exception) {
                        logger.warn("", e)
                        null
                    } finally {
                        MDC.remove(requestId)
                    }
                }
            }?.awaitAll()
        }
    }

    companion object {
        private val logger = LoggerFactory.getLogger(SpringFrameworkDomainEventSubscriber::class.java)
    }
}
