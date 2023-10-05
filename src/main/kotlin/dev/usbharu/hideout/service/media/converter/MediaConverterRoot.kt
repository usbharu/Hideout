package dev.usbharu.hideout.service.media.converter

import dev.usbharu.hideout.domain.model.hideout.dto.FileType
import dev.usbharu.hideout.domain.model.hideout.dto.ProcessedFile
import java.io.InputStream

interface MediaConverterRoot {
    suspend fun convert(
        fileType: FileType,
        contentType: String,
        filename: String,
        inputStream: InputStream
    ): ProcessedFile
}
