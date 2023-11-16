package dev.usbharu.hideout.core.service.media

import java.nio.file.Path

data class MediaSaveRequest(
    val name: String,
    val preffix: String,
    val filePath: Path,
    val thumbnailPath: Path?
)
