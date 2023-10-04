package dev.usbharu.hideout.service.media

interface FileTypeDeterminationService {
    fun fileType(byteArray: ByteArray, filename: String, contentType: String?): FileType

    enum class FileType {
        Image,
        Video,
        Audio,
        Unknown
    }
}
