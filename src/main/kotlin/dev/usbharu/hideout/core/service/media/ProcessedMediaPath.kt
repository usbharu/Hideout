package dev.usbharu.hideout.core.service.media

import java.nio.file.Path

data class ProcessedMediaPath(
    val filePath: Path,
    val thumbnailPath: Path?,
    val fileMimeType: MimeType,
    val thumbnailMimeType: MimeType?
)
