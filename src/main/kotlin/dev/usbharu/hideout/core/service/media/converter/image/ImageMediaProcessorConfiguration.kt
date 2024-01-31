package dev.usbharu.hideout.core.service.media.converter.image

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties("hideout.media.image")
data class ImageMediaProcessorConfiguration(
    val convert: String?,
    val thubnail: ImageMediaProcessorThumbnailConfiguration?,
    val supportedType: List<String>?,

)

data class ImageMediaProcessorThumbnailConfiguration(
    val generate: Boolean,
    val width: Int,
    val height: Int
)
