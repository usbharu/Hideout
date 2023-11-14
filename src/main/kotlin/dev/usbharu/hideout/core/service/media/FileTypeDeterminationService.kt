package dev.usbharu.hideout.core.service.media

import java.nio.file.Path

interface FileTypeDeterminationService {
    fun fileType(byteArray: ByteArray, filename: String, contentType: String?): MimeType
    fun fileType(path: Path, filename: String): MimeType
}
