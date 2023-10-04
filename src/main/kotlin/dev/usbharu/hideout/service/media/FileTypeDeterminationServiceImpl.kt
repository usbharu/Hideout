package dev.usbharu.hideout.service.media

import org.springframework.stereotype.Component

@Component
class FileTypeDeterminationServiceImpl : FileTypeDeterminationService {
    override fun fileType(
        byteArray: ByteArray,
        filename: String,
        contentType: String?
    ): FileTypeDeterminationService.FileType {
        if (contentType == null) {
            return FileTypeDeterminationService.FileType.Unknown
        }
        if (contentType.startsWith("image")) {
            return FileTypeDeterminationService.FileType.Image
        }
        if (contentType.startsWith("video")) {
            return FileTypeDeterminationService.FileType.Video
        }
        if (contentType.startsWith("audio")) {
            return FileTypeDeterminationService.FileType.Audio
        }
        return FileTypeDeterminationService.FileType.Unknown
    }
}
