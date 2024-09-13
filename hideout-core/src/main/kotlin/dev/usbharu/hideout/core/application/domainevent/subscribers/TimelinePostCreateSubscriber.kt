package dev.usbharu.hideout.core.application.domainevent.subscribers

import dev.usbharu.hideout.core.application.timeline.AddPost
import dev.usbharu.hideout.core.application.timeline.TimelineAddPostApplicationService
import dev.usbharu.hideout.core.domain.event.post.PostEvent
import dev.usbharu.hideout.core.domain.event.post.PostEventBody
import dev.usbharu.hideout.core.domain.model.support.principal.Anonymous
import dev.usbharu.hideout.core.domain.shared.domainevent.DomainEvent
import org.springframework.stereotype.Component

@Component
class TimelinePostCreateSubscriber(
    private val timelineAddPostApplicationService: TimelineAddPostApplicationService,
    private val domainEventSubscriber: DomainEventSubscriber,
) : Subscriber, DomainEventConsumer<PostEventBody> {
    override fun init() {
        domainEventSubscriber.subscribe<PostEventBody>(PostEvent.CREATE.eventName, this)
    }

    override suspend fun invoke(p1: DomainEvent<PostEventBody>) {
        timelineAddPostApplicationService.execute(AddPost(p1.body.getPostId()), Anonymous)
    }
}
