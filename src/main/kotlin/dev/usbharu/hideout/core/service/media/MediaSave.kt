package dev.usbharu.hideout.core.service.media

data class MediaSave(
    val name: String,
    val prefix: String,
    val fileInputStream: ByteArray,
    val thumbnailInputStream: ByteArray?
)
