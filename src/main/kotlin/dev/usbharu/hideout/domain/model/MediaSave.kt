package dev.usbharu.hideout.domain.model

import java.io.OutputStream

data class MediaSave(
    val name: String,
    val prefix: String,
    val fileInputStream: OutputStream,
    val thumbnailInputStream: OutputStream?
)
