package dev.usbharu.hideout.core.service.media.converter

import dev.usbharu.hideout.core.service.media.FileType
import dev.usbharu.hideout.core.service.media.ProcessedFile
import java.io.InputStream

interface MediaConverterRoot {
    suspend fun convert(
        fileType: FileType,
        contentType: String,
        filename: String,
        inputStream: InputStream
    ): ProcessedFile
}
