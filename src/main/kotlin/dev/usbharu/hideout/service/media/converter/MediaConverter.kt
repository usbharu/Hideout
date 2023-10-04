package dev.usbharu.hideout.service.media.converter

import dev.usbharu.hideout.domain.model.hideout.dto.FileType
import java.io.InputStream
import java.io.OutputStream

interface MediaConverter {
    fun isSupport(fileType: FileType): Boolean
    fun convert(inputStream: InputStream): OutputStream
}
