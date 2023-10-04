package dev.usbharu.hideout.service.media

import dev.usbharu.hideout.domain.model.hideout.dto.FileType

interface FileTypeDeterminationService {
    fun fileType(byteArray: ByteArray, filename: String, contentType: String?): FileType

}
