package dev.usbharu.hideout.core.application.timeline

import dev.usbharu.hideout.core.domain.model.timeline.TimelineVisibility

data class RegisterTimeline(
    val timelineName: String,
    val visibility: TimelineVisibility
)
