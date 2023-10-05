package dev.usbharu.hideout.service.media.converter

import dev.usbharu.hideout.domain.model.hideout.dto.FileType
import dev.usbharu.hideout.domain.model.hideout.dto.ProcessedFile
import java.io.InputStream

interface MediaConverter {
    fun isSupport(fileType: FileType): Boolean
    fun convert(inputStream: InputStream): ProcessedFile
}
