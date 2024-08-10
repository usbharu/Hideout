package dev.usbharu.hideout.core.application.domainevent.subscribers

import org.springframework.boot.ApplicationArguments
import org.springframework.boot.ApplicationRunner
import org.springframework.stereotype.Component

@Component
class SubscriberRunner(subscribers: List<Subscriber>) : ApplicationRunner {
    override fun run(args: ApplicationArguments?) {
    }
}
