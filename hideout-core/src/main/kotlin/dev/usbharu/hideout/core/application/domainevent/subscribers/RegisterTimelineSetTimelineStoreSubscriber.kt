package dev.usbharu.hideout.core.application.domainevent.subscribers

import dev.usbharu.hideout.core.application.timeline.SetTimelineToTimelineStoreApplicationService
import dev.usbharu.hideout.core.application.timeline.SetTimleineStore
import dev.usbharu.hideout.core.domain.event.timeline.TimelineEvent
import dev.usbharu.hideout.core.domain.event.timeline.TimelineEventBody
import dev.usbharu.hideout.core.domain.model.support.principal.Anonymous
import org.springframework.stereotype.Component

@Component
class RegisterTimelineSetTimelineStoreSubscriber(
    domainEventSubscriber: DomainEventSubscriber,
    private val setTimelineToTimelineStoreApplicationService: SetTimelineToTimelineStoreApplicationService
) :
    Subscriber {
    init {
        domainEventSubscriber.subscribe<TimelineEventBody>(TimelineEvent.CREATE.eventName) {
            setTimelineToTimelineStoreApplicationService.execute(SetTimleineStore(it.body.getTimelineId()), Anonymous)
        }
    }
}
