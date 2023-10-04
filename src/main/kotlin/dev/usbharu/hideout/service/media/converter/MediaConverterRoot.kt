package dev.usbharu.hideout.service.media.converter

import dev.usbharu.hideout.domain.model.hideout.dto.FileType
import java.io.InputStream

interface MediaConverterRoot {
    suspend fun convert(fileType: FileType, inputStream: InputStream): InputStream
}
