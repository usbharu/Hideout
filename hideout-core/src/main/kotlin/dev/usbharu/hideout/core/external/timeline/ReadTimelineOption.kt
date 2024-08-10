package dev.usbharu.hideout.core.external.timeline

data class ReadTimelineOption(
    val mediaOnly: Boolean = false,
    val local: Boolean = false,
    val remote: Boolean = false,
)
