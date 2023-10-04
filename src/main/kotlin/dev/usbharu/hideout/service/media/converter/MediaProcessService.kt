package dev.usbharu.hideout.service.media.converter

import dev.usbharu.hideout.domain.model.hideout.dto.FileType
import java.io.InputStream

interface MediaProcessService {
    suspend fun process(fileType: FileType, file: InputStream, thumbnail: InputStream?): Pair<InputStream, InputStream>
}
