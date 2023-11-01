package dev.usbharu.hideout.core.service.media.converter

import dev.usbharu.hideout.core.service.media.FileType
import dev.usbharu.hideout.core.service.media.ProcessedFile
import java.io.InputStream

interface MediaConverter {
    fun isSupport(fileType: FileType): Boolean
    fun convert(inputStream: InputStream): ProcessedFile
}
