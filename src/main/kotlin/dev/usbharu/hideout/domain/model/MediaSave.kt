package dev.usbharu.hideout.domain.model

data class MediaSave(
    val name: String,
    val prefix: String,
    val fileInputStream: ByteArray,
    val thumbnailInputStream: ByteArray?
)
