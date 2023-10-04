package dev.usbharu.hideout.domain.model

import java.io.InputStream

data class MediaSave(
    val name: String,
    val prefix: String,
    val fileInputStream: InputStream,
    val thumbnailInputStream: InputStream
)
