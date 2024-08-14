package dev.usbharu.hideout.core.application.timeline

import dev.usbharu.hideout.core.domain.model.support.page.Page

data class ReadTimeline(
    val timelineId: Long,
    val mediaOnly: Boolean,
    val localOnly: Boolean,
    val remoteOnly: Boolean,
    val page: Page
)
