package dev.usbharu.hideout.service.media

import dev.usbharu.hideout.domain.model.hideout.dto.ProcessedFile
import java.io.InputStream

interface ThumbnailGenerateService {
    fun generate(bufferedImage: InputStream, width: Int, height: Int): ProcessedFile?
    fun generate(outputStream: ByteArray, width: Int, height: Int): ProcessedFile?
}
