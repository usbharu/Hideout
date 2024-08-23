package dev.usbharu.hideout.core.application.domainevent.subscribers

import dev.usbharu.hideout.core.application.timeline.AddPost
import dev.usbharu.hideout.core.application.timeline.TimelineAddPostApplicationService
import dev.usbharu.hideout.core.domain.event.post.PostEvent
import dev.usbharu.hideout.core.domain.event.post.PostEventBody
import dev.usbharu.hideout.core.domain.model.support.principal.Anonymous
import org.springframework.stereotype.Component

@Component
class TimelinePostCreateSubscriber(
    private val timelineAddPostApplicationService: TimelineAddPostApplicationService,
    domainEventSubscriber: DomainEventSubscriber,
) : Subscriber {
    init {
        domainEventSubscriber.subscribe<PostEventBody>(PostEvent.CREATE.eventName) {
            timelineAddPostApplicationService.execute(AddPost(it.body.getPostId()), Anonymous)
        }
    }
}
