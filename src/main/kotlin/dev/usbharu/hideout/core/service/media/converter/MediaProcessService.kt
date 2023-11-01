package dev.usbharu.hideout.core.service.media.converter

import dev.usbharu.hideout.core.service.media.FileType
import dev.usbharu.hideout.core.service.media.ProcessedMedia

interface MediaProcessService {
    suspend fun process(
        fileType: FileType,
        contentType: String,
        fileName: String,
        file: ByteArray,
        thumbnail: ByteArray?
    ): ProcessedMedia
}
