package dev.usbharu.hideout.core.service.media.converter

import dev.usbharu.hideout.core.service.media.FileType
import dev.usbharu.hideout.core.service.media.MimeType
import dev.usbharu.hideout.core.service.media.ProcessedMedia
import dev.usbharu.hideout.core.service.media.ProcessedMediaPath
import java.nio.file.Path

interface MediaProcessService {
    fun isSupport(mimeType: MimeType): Boolean

    suspend fun process(
        fileType: FileType,
        contentType: String,
        fileName: String,
        file: ByteArray,
        thumbnail: ByteArray?
    ): ProcessedMedia

    suspend fun process(
        mimeType: MimeType,
        fileName: String,
        filePath: Path,
        thumbnails: Path?
    ): ProcessedMediaPath
}
