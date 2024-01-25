package dev.usbharu.hideout.application.config

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties("hideout.media")
data class MediaConfig(
    val remoteMediaFileSizeLimit: Long = 3000000L
)
