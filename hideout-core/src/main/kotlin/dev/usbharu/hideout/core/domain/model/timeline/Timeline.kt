package dev.usbharu.hideout.core.domain.model.timeline

import dev.usbharu.hideout.core.domain.event.timeline.TimelineEvent
import dev.usbharu.hideout.core.domain.event.timeline.TimelineEventFactory
import dev.usbharu.hideout.core.domain.model.userdetails.UserDetail
import dev.usbharu.hideout.core.domain.model.userdetails.UserDetailId
import dev.usbharu.hideout.core.domain.shared.domainevent.DomainEventStorable

class Timeline(
    val id: TimelineId,
    val userDetailId: UserDetailId,
    name: TimelineName,
    visibility: TimelineVisibility,
    val isSystem: Boolean
) : DomainEventStorable() {
    var visibility = visibility
        private set

    fun setVisibility(visibility: TimelineVisibility, userDetail: UserDetail) {
        check(isSystem.not())
        require(userDetailId == userDetail.id)
        this.visibility = visibility
        addDomainEvent(TimelineEventFactory(this).createEvent(TimelineEvent.CHANGE_VISIBILITY))
    }

    var name = name
        private set
}