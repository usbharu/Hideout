package dev.usbharu.hideout.core.application.domainevent.subscribers

import dev.usbharu.hideout.core.application.timeline.SetTimelineToTimelineStoreApplicationService
import dev.usbharu.hideout.core.application.timeline.SetTimleineStore
import dev.usbharu.hideout.core.domain.event.timeline.TimelineEvent
import dev.usbharu.hideout.core.domain.event.timeline.TimelineEventBody
import dev.usbharu.hideout.core.domain.model.support.principal.Anonymous
import dev.usbharu.hideout.core.domain.shared.domainevent.DomainEvent
import org.springframework.stereotype.Component

@Component
class RegisterTimelineSetTimelineStoreSubscriber(
    private val domainEventSubscriber: DomainEventSubscriber,
    private val setTimelineToTimelineStoreApplicationService: SetTimelineToTimelineStoreApplicationService
) : Subscriber, DomainEventConsumer<TimelineEventBody> {

    override fun init() {
        domainEventSubscriber.subscribe<TimelineEventBody>(TimelineEvent.CREATE.eventName, this)
    }

    override suspend fun invoke(p1: DomainEvent<TimelineEventBody>) {
        setTimelineToTimelineStoreApplicationService.execute(SetTimleineStore(p1.body.getTimelineId()), Anonymous)
    }
}
