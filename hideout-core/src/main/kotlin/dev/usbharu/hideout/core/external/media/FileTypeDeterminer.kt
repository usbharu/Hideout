package dev.usbharu.hideout.core.external.media

import dev.usbharu.hideout.core.domain.model.media.MimeType
import java.nio.file.Path

interface FileTypeDeterminer {
    fun fileType(path: Path, filename: String): MimeType
}
