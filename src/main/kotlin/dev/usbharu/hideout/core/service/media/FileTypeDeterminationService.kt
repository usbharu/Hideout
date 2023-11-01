package dev.usbharu.hideout.core.service.media

interface FileTypeDeterminationService {
    fun fileType(byteArray: ByteArray, filename: String, contentType: String?): FileType
}
