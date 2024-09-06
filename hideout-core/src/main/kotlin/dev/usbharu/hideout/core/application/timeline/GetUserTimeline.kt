package dev.usbharu.hideout.core.application.timeline

import dev.usbharu.hideout.core.domain.model.support.page.Page

data class GetUserTimeline(val id: Long, val page: Page)
