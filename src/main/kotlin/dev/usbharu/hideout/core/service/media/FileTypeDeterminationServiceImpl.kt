package dev.usbharu.hideout.core.service.media

import org.springframework.stereotype.Component

@Component
class FileTypeDeterminationServiceImpl : FileTypeDeterminationService {
    override fun fileType(
        byteArray: ByteArray,
        filename: String,
        contentType: String?
    ): FileType {
        if (contentType == null) {
            return FileType.Unknown
        }
        if (contentType.startsWith("image")) {
            return FileType.Image
        }
        if (contentType.startsWith("video")) {
            return FileType.Video
        }
        if (contentType.startsWith("audio")) {
            return FileType.Audio
        }
        return FileType.Unknown
    }
}
