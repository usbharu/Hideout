package dev.usbharu.hideout.core.application.domainevent.subscribers

import dev.usbharu.hideout.core.domain.event.post.PostEvent
import dev.usbharu.hideout.core.domain.event.post.PostEventBody
import org.slf4j.LoggerFactory
import org.springframework.context.annotation.Scope
import org.springframework.stereotype.Component

@Component
class TimelinePostCreateSubscriber(domainEventSubscriber: DomainEventSubscriber) :Subscriber{
    init {
        domainEventSubscriber.subscribe<PostEventBody>(PostEvent.CREATE.eventName) {
            val post = it.body.getPost()
            val actor = it.body.getActor()

            logger.info("New Post! : {}",post)

        }
    }

    companion object{
        private val logger = LoggerFactory.getLogger(TimelinePostCreateSubscriber::class.java)
    }
}
