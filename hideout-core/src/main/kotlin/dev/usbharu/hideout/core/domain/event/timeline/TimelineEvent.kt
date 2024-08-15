package dev.usbharu.hideout.core.domain.event.timeline

import dev.usbharu.hideout.core.domain.model.timeline.Timeline
import dev.usbharu.hideout.core.domain.model.timeline.TimelineId
import dev.usbharu.hideout.core.domain.shared.domainevent.DomainEvent
import dev.usbharu.hideout.core.domain.shared.domainevent.DomainEventBody

class TimelineEventFactory(private val timeline: Timeline) {
    fun createEvent(timelineEvent: TimelineEvent): DomainEvent<TimelineEventBody> =
        DomainEvent.create(timelineEvent.eventName, TimelineEventBody(timeline.id))
}

class TimelineEventBody(timelineId: TimelineId) : DomainEventBody(mapOf("timeline" to timelineId)) {
    fun getTimelineId(): TimelineId {
        return toMap()["timeline"] as TimelineId
    }
}

enum class TimelineEvent(val eventName: String) {
    CHANGE_VISIBILITY("ChangeVisibility"),
    CREATE("TimelineCreate")
}
