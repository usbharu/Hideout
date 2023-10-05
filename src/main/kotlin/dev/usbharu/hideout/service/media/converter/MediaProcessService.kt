package dev.usbharu.hideout.service.media.converter

import dev.usbharu.hideout.domain.model.hideout.dto.FileType
import dev.usbharu.hideout.domain.model.hideout.dto.ProcessedMedia

interface MediaProcessService {
    suspend fun process(
        fileType: FileType,
        contentType: String,
        fileName: String,
        file: ByteArray,
        thumbnail: ByteArray?
    ): ProcessedMedia
}
