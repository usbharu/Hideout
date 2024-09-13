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

    var name = name
        private set

    fun setVisibility(visibility: TimelineVisibility, userDetail: UserDetail) {
        check(isSystem.not())
        require(userDetailId == userDetail.id)
        this.visibility = visibility
        addDomainEvent(TimelineEventFactory(this).createEvent(TimelineEvent.CHANGE_VISIBILITY))
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Timeline

        return id == other.id
    }

    override fun hashCode(): Int {
        return id.hashCode()
    }

    companion object {
        fun create(
            id: TimelineId,
            userDetailId: UserDetailId,
            name: TimelineName,
            visibility: TimelineVisibility,
            isSystem: Boolean
        ): Timeline {
            val timeline = Timeline(
                id = id,
                userDetailId = userDetailId,
                name = name,
                visibility = visibility,
                isSystem = isSystem
            )
            timeline.addDomainEvent(TimelineEventFactory(timeline).createEvent(TimelineEvent.CREATE))
            return timeline
        }
    }
}
