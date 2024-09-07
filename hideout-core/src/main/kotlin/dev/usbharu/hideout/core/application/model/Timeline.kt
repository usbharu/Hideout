package dev.usbharu.hideout.core.application.model

import dev.usbharu.hideout.core.domain.model.timeline.TimelineVisibility

data class Timeline(
    val id: Long,
    val userDetailId: Long,
    val name: String,
    val visibility: TimelineVisibility,
    val isSystem: Boolean
) {
    companion object {
        fun of(timeline: dev.usbharu.hideout.core.domain.model.timeline.Timeline): Timeline {
            return Timeline(
                timeline.id.value,
                timeline.userDetailId.id,
                timeline.name.value,
                timeline.visibility,
                timeline.isSystem
            )
        }
    }
}
