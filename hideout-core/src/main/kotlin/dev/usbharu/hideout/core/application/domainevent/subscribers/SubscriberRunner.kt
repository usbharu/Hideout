package dev.usbharu.hideout.core.application.domainevent.subscribers

import org.slf4j.LoggerFactory
import org.springframework.boot.ApplicationArguments
import org.springframework.boot.ApplicationRunner
import org.springframework.stereotype.Component

@Component
class SubscriberRunner(
    private val subscribers: List<Subscriber>,
    private val domainEventSubscriber: DomainEventSubscriber
) : ApplicationRunner {
    override fun run(args: ApplicationArguments?) {
        subscribers.forEach { it.init() }

        if (logger.isDebugEnabled) {
            val stringListMap = domainEventSubscriber.getSubscribers()

            val header = """
                |====== Domain Event Subscribers Report =====
                |
                |
                |
            """.trimMargin()

            val value = stringListMap.map {
                it.key + "\n\t" + it.value.joinToString("\n", "[", "]") { suspendFunction1 ->
                    suspendFunction1::class.qualifiedName.orEmpty()
                }
            }.joinToString("\n\n\n")

            val footer = """
                |
                |
                |
                |===== Domain Event Subscribers Report =====
            """.trimMargin()

            logger.debug("{}{}{}", header, value, footer)
        }

    }

    companion object {
        private val logger = LoggerFactory.getLogger(SubscriberRunner::class.java)
    }
}
