package dev.usbharu.hideout.core.config

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties("hideout.timeline.default")
data class DefaultTimelineStoreConfig(
    val actorPostsCount: Int = 500
)
