package dev.usbharu.hideout.service.media.converter

import dev.usbharu.hideout.domain.model.hideout.dto.FileType
import java.io.InputStream
import java.io.OutputStream

interface MediaProcessService {
    suspend fun process(
        fileType: FileType,
        file: InputStream,
        thumbnail: InputStream?
    ): Pair<OutputStream, OutputStream>
}
