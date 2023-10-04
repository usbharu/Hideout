package dev.usbharu.hideout.service.media.converter

import dev.usbharu.hideout.domain.model.hideout.dto.FileType

interface MediaProcessService {
    suspend fun process(
        fileType: FileType,
        file: ByteArray,
        thumbnail: ByteArray?
    ): Pair<ByteArray, ByteArray>
}
