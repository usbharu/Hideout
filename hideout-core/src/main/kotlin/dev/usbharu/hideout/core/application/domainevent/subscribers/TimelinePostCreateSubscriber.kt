package dev.usbharu.hideout.core.application.domainevent.subscribers

import dev.usbharu.hideout.core.domain.event.post.PostEvent
import dev.usbharu.hideout.core.domain.event.post.PostEventBody
import dev.usbharu.hideout.core.external.timeline.TimelineStore
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component

@Component
class TimelinePostCreateSubscriber(
    private val timelineStore: TimelineStore,
    domainEventSubscriber: DomainEventSubscriber
) : Subscriber {
    init {
        domainEventSubscriber.subscribe<PostEventBody>(PostEvent.CREATE.eventName) {
            timelineStore.addPost(it.body.getPost())
        }
    }

    companion object {
        private val logger = LoggerFactory.getLogger(TimelinePostCreateSubscriber::class.java)
    }
}
