package dev.usbharu.hideout.core.config

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties("hideout.media.image.imageio")
data class ImageIOImageConfig(
    val thumbnailsWidth: Int = 1000,
    val thumbnailsHeight: Int = 1000,
    val format: String = "jpeg"
)
