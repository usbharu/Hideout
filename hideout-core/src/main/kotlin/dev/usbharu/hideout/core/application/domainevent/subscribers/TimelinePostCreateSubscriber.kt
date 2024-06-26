package dev.usbharu.hideout.core.application.domainevent.subscribers

import dev.usbharu.hideout.core.domain.event.post.PostEvent
import dev.usbharu.hideout.core.domain.event.post.PostEventBody
import org.springframework.stereotype.Component

@Component
class TimelinePostCreateSubscriber(domainEventSubscriber: DomainEventSubscriber) {
    init {
        domainEventSubscriber.subscribe<PostEventBody>(PostEvent.CREATE.eventName) {
            val post = it.body.getPost()
            val actor = it.body.getActor()

            println(post.toString())
        }
    }
}
